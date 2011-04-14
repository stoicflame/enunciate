package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;


public class AMFUtils {
  /**
   * Find the specified property for the given class.
   *
   * @param clazz The class.
   * @param property The property.
   * @return The property descriptor.
   */
  public static PropertyDescriptor findProperty(Class clazz, final String property) {
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
   * Find the type adapter for the specified JAXB property.
   *
   * @param jaxbProperty The JAXB property for which to find a type adapter.
   * @return The type adapter, or null if none was found.
   */
  public static XmlJavaTypeAdapter findTypeAdapter(PropertyDescriptor jaxbProperty) {
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
  public static XmlElement findXmlElement(PropertyDescriptor property){
    XmlElement xmlElement = null;

    if (property.getReadMethod() != null) {
      xmlElement = property.getReadMethod().getAnnotation(XmlElement.class);
    }

    if ((xmlElement == null) && (property.getWriteMethod() != null)) {
      xmlElement = property.getWriteMethod().getAnnotation(XmlElement.class);
    }

    return xmlElement;
  }
}
