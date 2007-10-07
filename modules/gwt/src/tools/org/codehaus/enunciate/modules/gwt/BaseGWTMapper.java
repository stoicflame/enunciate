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

package org.codehaus.enunciate.modules.gwt;

import org.springframework.beans.BeanUtils;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Default implementation of a GWTMapper.
 *
 * @author Ryan Heaton
 */
public abstract class BaseGWTMapper<J, G> implements GWTMapper<J, G> {

  private final Class<J> jaxbClass;
  private final Class<G> gwtClass;
  private final String[] properties;
  private final PropertyDescriptor[][] jaxbProperties2gwtProperties;

  protected BaseGWTMapper(Class<J> jaxbClass, Class<G> gwtClass, String... properties) {
    this.jaxbClass = jaxbClass;
    this.gwtClass = gwtClass;
    if (properties == null) {
      properties = new String[0];
    }

    this.properties = properties;
    this.jaxbProperties2gwtProperties = new PropertyDescriptor[this.properties.length][];
    for (int i = 0; i < properties.length; i++) {
      String property = properties[i];
      PropertyDescriptor jaxbProperty = BeanUtils.getPropertyDescriptor(jaxbClass, property);
      if (jaxbProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + jaxbClass.getName() + ".");
      }

      PropertyDescriptor gwtProperty = BeanUtils.getPropertyDescriptor(gwtClass, property);
      if (gwtProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + gwtClass.getName() + ".");
      }

      this.jaxbProperties2gwtProperties[i] = new PropertyDescriptor[]{jaxbProperty, gwtProperty};
    }
  }

  public G toGWT(J jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (context.getMappedObjects().containsKey(jaxbObject)) {
      return (G) context.getMappedObjects().get(jaxbObject);
    }

    G gwtObject;
    try {
      gwtObject = gwtClass.newInstance();
    }
    catch (Exception e) {
      throw new GWTMappingException("Unable to instantiate an instance GWT class " + gwtClass.getName() + ".", e);
    }

    context.objectMapped(jaxbObject, gwtObject);
    for (PropertyDescriptor[] pds : this.jaxbProperties2gwtProperties) {
      PropertyDescriptor jaxbProperty = pds[0];
      PropertyDescriptor gwtProperty = pds[1];
      Method getter = jaxbProperty.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(jaxbObject);
      }
      catch (Exception e) {
        throw new GWTMappingException("Unable to read property '" + jaxbProperty.getName() + "' on " + jaxbObject, e);
      }

      if (propertyValue == null) {
        continue;
      }

      XmlJavaTypeAdapter adapterInfo = findTypeAdapter(jaxbProperty);
      GWTMapper mapper = GWTMapperIntrospector.getGWTMapper(getter.getGenericReturnType(), adapterInfo);
      try {
        gwtProperty.getWriteMethod().invoke(gwtObject, mapper.toGWT(propertyValue, context));
      }
      catch (Exception e) {
        throw new GWTMappingException("Unable to set property " + jaxbProperty.getName() + " for the gwt bean " + gwtClass.getName(), e);
      }
    }

    return gwtObject;
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

  public J toJAXB(G gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (context.getMappedObjects().containsKey(gwtObject)) {
      return (J) context.getMappedObjects().get(gwtObject);
    }

    J jaxbObject;
    try {
      jaxbObject = jaxbClass.newInstance();
    }
    catch (Exception e) {
      throw new GWTMappingException("Unable to instantiate an instance JAXB class " + jaxbClass.getName() + ".", e);
    }

    for (PropertyDescriptor[] pds : this.jaxbProperties2gwtProperties) {
      PropertyDescriptor jaxbProperty = pds[0];
      PropertyDescriptor gwtProperty = pds[1];
      Method getter = gwtProperty.getReadMethod();
      Object propertyValue;
      try {
        propertyValue = getter.invoke(gwtObject);
      }
      catch (Exception e) {
        throw new GWTMappingException("Unable to read property '" + gwtProperty.getName() + "' on " + gwtObject, e);
      }

      if (propertyValue == null) {
        continue;
      }

      GWTMapper mapper = GWTMapperIntrospector.getGWTMapper(jaxbProperty.getReadMethod().getGenericReturnType(), findTypeAdapter(jaxbProperty));
      try {
        jaxbProperty.getWriteMethod().invoke(jaxbObject, mapper.toJAXB(propertyValue, context));
      }
      catch (Exception e) {
        throw new GWTMappingException("Unable to set property " + jaxbProperty.getName() + " for the gwt bean " + gwtClass.getName(), e);
      }
    }

    context.objectMapped(gwtObject, jaxbObject);

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
