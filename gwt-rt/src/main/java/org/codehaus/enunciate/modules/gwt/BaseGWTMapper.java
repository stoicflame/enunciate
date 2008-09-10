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

package org.codehaus.enunciate.modules.gwt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Base implementation of an GWTMapper. If a custom mapper exists for a certain JAXB class, it is assumed to
 * exist in the "gwt" package relative to the JAXB class package, same name, with "GWTMapper" appended to the
 * class name. Since it's generated, it will likely extend this base mapper class.
 *
 * @author Ryan Heaton
 */
public abstract class BaseGWTMapper<J, G> implements CustomGWTMapper<J, G> {

  private final Class<J> jaxbClass;
  private final Class<G> gwtClass;
  private final String[] properties;
  private final PropertyDescriptor[][] jaxbProperties2gwtProperties;

  /**
   * Construct a base GWT mapper.
   *
   * @param jaxbClass The JAXB class to map.
   * @param gwtClass The GWT class to map.
   * @param properties The properties of the JAXB class that will be mapped to corresponding properties of the GWT class.
   */
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
      PropertyDescriptor jaxbProperty = findProperty(jaxbClass, property);
      if (jaxbProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + jaxbClass.getName() + ".");
      }

      PropertyDescriptor gwtProperty = findProperty(gwtClass, property);
      if (gwtProperty == null) {
        throw new IllegalStateException("Unknown property '" + property + "' on class " + gwtClass.getName() + ".");
      }

      this.jaxbProperties2gwtProperties[i] = new PropertyDescriptor[]{jaxbProperty, gwtProperty};
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
   * Map a JAXB object to an GWT object.
   *
   * @param jaxbObject The JAXB object.
   * @param context The mapping context.
   * @return The mapped GWT object.
   */
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
      if (getter == null) {
        throw new GWTMappingException("In order to convert from JAXB classes to GWT, you must provide a getter for property '"
          + jaxbProperty.getName() + "' on class " + jaxbProperty.getWriteMethod().getDeclaringClass());
      }

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

      GWTMapper mapper = GWTMapperIntrospector.getGWTMapper(propertyValue.getClass(), getter.getGenericReturnType(),
                                                            findTypeAdapter(jaxbProperty), findXmlElement(jaxbProperty));
      try {
        gwtProperty.getWriteMethod().invoke(gwtObject, mapper.toGWT(propertyValue, context));
      }
      catch (Exception e) {
        throw new GWTMappingException("Unable to set property " + jaxbProperty.getName() + " for the gwt bean " + gwtClass.getName(), e);
      }
    }

    return gwtObject;
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
      Package pckg;
      Class<?> propertyType;
      if (jaxbProperty.getReadMethod() != null) {
        pckg = jaxbProperty.getReadMethod().getDeclaringClass().getPackage();
        propertyType = jaxbProperty.getReadMethod().getReturnType();
      }
      else {
        pckg = jaxbProperty.getWriteMethod().getDeclaringClass().getPackage();
        propertyType = jaxbProperty.getWriteMethod().getParameterTypes()[0];
      }

      XmlJavaTypeAdapter possibleAdapterInfo = pckg.getAnnotation(XmlJavaTypeAdapter.class);
      if ((possibleAdapterInfo != null) && (propertyType.equals(possibleAdapterInfo.type()))) {
        adapterInfo = possibleAdapterInfo;
      }
      else if (pckg.isAnnotationPresent(XmlJavaTypeAdapters.class)) {
        XmlJavaTypeAdapters adapters = pckg.getAnnotation(XmlJavaTypeAdapters.class);
        for (XmlJavaTypeAdapter possibility : adapters.value()) {
          if (propertyType.equals(possibility.type())) {
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
   * Map an GWT object to a JAXB object.
   *
   * @param gwtObject The GWT object to map.
   * @param context The mapping context.
   * @return The JAXB object.
   */
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

      GWTMapper mapper = GWTMapperIntrospector.getGWTMapperForGWTObject(propertyValue);
      Method setter = jaxbProperty.getWriteMethod();
      if (setter == null) {
        throw new GWTMappingException("In order to convert from GWT back to JAXB classes, you must provide a setter for property '"
          + jaxbProperty.getName() + "' on class " + jaxbProperty.getReadMethod().getDeclaringClass());
      }
      
      if (mapper == null) {
        mapper = GWTMapperIntrospector.getGWTMapper(setter.getGenericParameterTypes()[0], findTypeAdapter(jaxbProperty), findXmlElement(jaxbProperty));
      }

      try {
        setter.invoke(jaxbObject, mapper.toJAXB(propertyValue, context));
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

  /**
   * The JAXB class applicable to this mapper.
   *
   * @return The JAXB class applicable to this mapper.
   */
  public Class<J> getJaxbClass() {
    return jaxbClass;
  }

  /**
   * The GWT class applicable to this mapper.
   *
   * @return The GWT class applicable to this mapper.
   */
  public Class<G> getGwtClass() {
    return gwtClass;
  }
}
