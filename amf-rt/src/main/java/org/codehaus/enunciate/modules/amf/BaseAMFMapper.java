/*
 * Copyright 2006-2008 Web Cohesion
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Base implementation of an AMFMapper. If a custom mapper exists for a certain JAXB class, it is assumed to
 * exist in the "amf" package relative to the JAXB class package, same name, with "AMFMapper" appended to the
 * class name. Since it's generated, it will likely extend this base mapper class.
 *
 * @author Ryan Heaton
 */
public abstract class BaseAMFMapper<J, G> implements CustomAMFMapper<J, G> {

  private final Class<J> jaxbClass;
  private final Class<G> amfClass;
  private final String[] properties;
  private final PropertyDescriptor[][] jaxbProperties2amfProperties;

  /**
   * Construct a base AMF mapper.
   *
   * @param jaxbClass The JAXB class to map.
   * @param amfClass The AMF class to map.
   * @param properties The properties of the JAXB class that will be mapped to corresponding properties of the AMF class.
   */
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
      PropertyDescriptor jaxbProperty = findProperty(jaxbClass, property);
      if (jaxbProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + jaxbClass.getName() + ".");
      }

      PropertyDescriptor amfProperty = findProperty(amfClass, property);
      if (amfProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + amfClass.getName() + ".");
      }

      this.jaxbProperties2amfProperties[i] = new PropertyDescriptor[]{jaxbProperty, amfProperty};
    }
  }

  /**
   * Find the specified property for the given class.
   *
   * @param clazz The class.
   * @param property The property.
   * @return The property descriptor.
   */
  protected PropertyDescriptor findProperty(Class clazz, final String property) {
    if (Object.class.equals(clazz)) {
      return null;
    }

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(clazz);
    }
    catch (IntrospectionException e) {
      throw new IllegalStateException(e);
    }
    
    PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
    for (PropertyDescriptor pd : pds) {
      if (pd.getName().equals(property)) {
        return pd;
      }
    }

    return findProperty(clazz.getSuperclass(), property);
  }

  /**
   * Map a JAXB object to an AMF object.
   *
   * @param jaxbObject The JAXB object.
   * @param context The mapping context.
   * @return The mapped AMF object.
   */
  public G toAMF(J jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

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
      if (getter == null) {
        throw new AMFMappingException("In order to convert from JAXB classes to AMF, you must provide a getter for property '"
          + jaxbProperty.getName() + "' on class " + jaxbProperty.getWriteMethod().getDeclaringClass());
      }

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
      XmlElement xmlElement = findXmlElement(jaxbProperty);
      AMFMapper mapper = AMFMapperIntrospector.getAMFMapper(propertyValue.getClass(), getter.getGenericReturnType(), adapterInfo, xmlElement);
      try {
        amfProperty.getWriteMethod().invoke(amfObject, mapper.toAMF(propertyValue, context));
      }
      catch (Exception e) {
        throw new AMFMappingException("Unable to set property " + jaxbProperty.getName() + " for the amf bean " + amfClass.getName(), e);
      }
    }

    return amfObject;
  }

  /**
   * Find the type adapter for the specified JAXB property.
   *
   * @param jaxbProperty The JAXB property for which to find a type adapter.
   * @return The type adapter, or null if none was found.
   */
  private XmlJavaTypeAdapter findTypeAdapter(PropertyDescriptor jaxbProperty) {
    XmlJavaTypeAdapter adapterInfo = null;

    if (jaxbProperty.getReadMethod() != null) {
      adapterInfo = jaxbProperty.getReadMethod().getAnnotation(XmlJavaTypeAdapter.class);
    }

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

  /**
   * Find the xml element metadata for a specified JAXB property.
   *
   * @param property The JAXB property for which to find the xml element metadata.
   * @return The xml element metadata, or null if none found.
   */
  private XmlElement findXmlElement(PropertyDescriptor property){
    XmlElement xmlElement = null;

    if (property.getReadMethod() != null) {
      xmlElement = property.getReadMethod().getAnnotation(XmlElement.class);
    }

    if ((xmlElement == null) && (property.getWriteMethod() != null)) {
      xmlElement = property.getWriteMethod().getAnnotation(XmlElement.class);
    }

    return xmlElement;
  }

  /**
   * Map an AMF object to a JAXB object.
   *
   * @param amfObject The AMF object to map.
   * @param context The mapping context.
   * @return The JAXB object.
   */
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

      Method setter = jaxbProperty.getWriteMethod();
      if (setter == null) {
        throw new AMFMappingException("In order to convert from AMF back to JAXB classes, you must provide a setter for property '"
          + jaxbProperty.getName() + "' on class " + jaxbProperty.getReadMethod().getDeclaringClass());
      }

      AMFMapper mapper;
      if (propertyValue instanceof AMFMapperAware) {
        mapper = ((AMFMapperAware) propertyValue).loadAMFMapper();
      }
      else {
        mapper = AMFMapperIntrospector.getAMFMapper(setter.getGenericParameterTypes()[0], findTypeAdapter(jaxbProperty), findXmlElement(jaxbProperty));
      }
      
      try {
        setter.invoke(jaxbObject, mapper.toJAXB(propertyValue, context));
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

  /**
   * The JAXB class applicable to this mapper.
   *
   * @return The JAXB class applicable to this mapper.
   */
  public Class<J> getJaxbClass() {
    return jaxbClass;
  }

  /**
   * The AMF class applicable to this mapper.
   *
   * @return The AMF class applicable to this mapper.
   */
  public Class<G> getAmfClass() {
    return amfClass;
  }
}
