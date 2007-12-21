/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.amf;

import org.springframework.beans.BeanUtils;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Default implementation of a AMFMapper.
 *
 * @author Ryan Heaton
 */
public abstract class BaseAMFMapper<J, G> implements AMFMapper<J, G> {

  private final Class<J> jaxbClass;
  private final Class<G> amfClass;
  private final String[] properties;
  private final PropertyDescriptor[][] jaxbProperties2amfProperties;

  protected BaseAMFMapper(Class<J> jaxbClass, Class<G> amfClass, String... properties) {
    this.jaxbClass = jaxbClass;
    this.amfClass = amfClass;
    if (properties == null) {
      properties = new String[0];
    }

    this.properties = properties;
    this.jaxbProperties2amfProperties = new PropertyDescriptor[this.properties.length][];
    for (int i = 0; i < properties.length; i++) {
      String property = properties[i];
      PropertyDescriptor jaxbProperty = BeanUtils.getPropertyDescriptor(jaxbClass, property);
      if (jaxbProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + jaxbClass.getName() + ".");
      }

      PropertyDescriptor amfProperty = BeanUtils.getPropertyDescriptor(amfClass, property);
      if (amfProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + amfClass.getName() + ".");
      }

      this.jaxbProperties2amfProperties[i] = new PropertyDescriptor[]{jaxbProperty, amfProperty};
    }
  }

  public G toAMF(J jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (context.getMappedObjects().containsKey(jaxbObject)) {
      return (G) context.getMappedObjects().get(jaxbObject);
    }

    G amfObject;
    try {
      amfObject = amfClass.newInstance();
    }
    catch (Exception e) {
      throw new AMFMappingException("Unable to instantiate an instance AMF class " + amfClass.getName() + ".", e);
    }

    context.objectMapped(jaxbObject, amfObject);
    for (PropertyDescriptor[] pds : this.jaxbProperties2amfProperties) {
      PropertyDescriptor jaxbProperty = pds[0];
      PropertyDescriptor amfProperty = pds[1];
      Method getter = jaxbProperty.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(jaxbObject);
      }
      catch (Exception e) {
        throw new AMFMappingException("Unable to read property '" + jaxbProperty.getName() + "' on " + jaxbObject, e);
      }

      if (propertyValue == null) {
        continue;
      }

      XmlJavaTypeAdapter adapterInfo = findTypeAdapter(jaxbProperty);
      AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(getter.getGenericReturnType(), adapterInfo);
      try {
        amfProperty.getWriteMethod().invoke(amfObject, mapper.toAMF(propertyValue, context));
      }
      catch (Exception e) {
        throw new AMFMappingException("Unable to set property " + jaxbProperty.getName() + " for the amf bean " + amfClass.getName(), e);
      }
    }

    return amfObject;
  }

  private XmlJavaTypeAdapter findTypeAdapter(PropertyDescriptor jaxbProperty) {
    XmlJavaTypeAdapter adapterInfo = jaxbProperty.getReadMethod().getAnnotation(XmlJavaTypeAdapter.class);

    if ((adapterInfo == null) && (jaxbProperty.getWriteMethod() != null)) {
      adapterInfo = jaxbProperty.getWriteMethod().getAnnotation(XmlJavaTypeAdapter.class);
    }

    if (adapterInfo == null) {
      Package pckg = jaxbProperty.getReadMethod().getDeclaringClass().getPackage();
      Class<?> returnType = jaxbProperty.getReadMethod().getReturnType();

      XmlJavaTypeAdapter possibleAdapterInfo = pckg.getAnnotation(XmlJavaTypeAdapter.class);
      if ((possibleAdapterInfo != null) && (returnType.equals(possibleAdapterInfo.type()))) {
        adapterInfo = possibleAdapterInfo;
      }
      else if (pckg.isAnnotationPresent(XmlJavaTypeAdapters.class)) {
        XmlJavaTypeAdapters adapters = pckg.getAnnotation(XmlJavaTypeAdapters.class);
        for (XmlJavaTypeAdapter possibility : adapters.value()) {
          if (returnType.equals(possibility.type())) {
            adapterInfo = possibility;
          }
        }
      }
    }

    return adapterInfo;
  }

  public J toJAXB(G amfObject, AMFMappingContext context) throws AMFMappingException {
    if (context.getMappedObjects().containsKey(amfObject)) {
      return (J) context.getMappedObjects().get(amfObject);
    }

    J jaxbObject;
    try {
      jaxbObject = jaxbClass.newInstance();
    }
    catch (Exception e) {
      throw new AMFMappingException("Unable to instantiate an instance JAXB class " + jaxbClass.getName() + ".", e);
    }

    for (PropertyDescriptor[] pds : this.jaxbProperties2amfProperties) {
      PropertyDescriptor jaxbProperty = pds[0];
      PropertyDescriptor amfProperty = pds[1];
      Method getter = amfProperty.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(amfObject);
      }
      catch (Exception e) {
        throw new AMFMappingException("Unable to read property '" + amfProperty.getName() + "' on " + amfObject, e);
      }

      if (propertyValue == null) {
        continue;
      }

      AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(jaxbProperty.getReadMethod().getGenericReturnType(), findTypeAdapter(jaxbProperty));
      try {
        jaxbProperty.getWriteMethod().invoke(jaxbObject, mapper.toJAXB(propertyValue, context));
      }
      catch (Exception e) {
        throw new AMFMappingException("Unable to set property " + jaxbProperty.getName() + " for the amf bean " + amfClass.getName(), e);
      }
    }

    context.objectMapped(amfObject, jaxbObject);

    return jaxbObject;
  }

  /**
   * Utility for appending one string array to another.
   * @param args1 The first set of args.
   * @param args2 The second set of args.
   * @return The appended array.
   */
  public static String[] append(String[] args1, String... args2) {
    String[] allArgs = new String[args1.length + args2.length];
    System.arraycopy(args2, 0, allArgs, 0, args2.length);
    System.arraycopy(args1, 0, allArgs, args2.length, args1.length);
    return allArgs;
  }

}
