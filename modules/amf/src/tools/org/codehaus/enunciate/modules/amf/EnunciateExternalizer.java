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

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.Converter;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.springframework.beans.BeanUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Basic externalizer that externalizes JAXB properties.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateExternalizer implements Externalizer {

  private final Class jaxbClass;
  private final PropertyDescriptor[] externalizableProperties;
  private final XmlJavaTypeAdapter[] typeAdapters;

  protected EnunciateExternalizer(Class jaxbClass, String... properties) {
    this.jaxbClass = jaxbClass;
    this.externalizableProperties = new PropertyDescriptor[properties.length];
    this.typeAdapters = new XmlJavaTypeAdapter[properties.length];
    for (int i = 0; i < properties.length; i++) {
      PropertyDescriptor jaxbProperty = BeanUtils.getPropertyDescriptor(jaxbClass, properties[i]);
      if (jaxbProperty == null) {
        throw new IllegalStateException("Unknown property '" + properties[i] + "' on class " + jaxbClass.getName() + ".");
      }
      this.externalizableProperties[i] = jaxbProperty;
      this.typeAdapters[i] = findTypeAdapter(jaxbProperty);
    }
  }

  /**
   * Creates an instance of the JAXB class.
   *
   * @param type The name of the JAXB class.
   * @param in   The input.
   * @return The new instance.
   */
  public Object newInstance(String type, ObjectInput in) throws IllegalAccessException, InstantiationException {
    if (!jaxbClass.getName().equals(type)) {
      throw new InstantiationException("Unsupported type: " + type + ".  Expected " + jaxbClass.getName() + ".");
    }

    return jaxbClass.newInstance();
  }

  /**
   * Reads and sets the properties of the jaxb object.
   *
   * @param o  The object.
   * @param in The input stream.
   */
  public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
    Converter converter = GraniteContext.getCurrentInstance().getGraniteConfig().getConverter();
    for (int i = 0; i < externalizableProperties.length; i++) {
      PropertyDescriptor property = externalizableProperties[i];
      try {
        Object value;
        XmlJavaTypeAdapter typeAdapter = typeAdapters[i];
        if (typeAdapter != null) {
          Type adaptingType = findAdaptingType(typeAdapter.value());
          value = converter.convertForDeserialization(in.readObject(), adaptingType);
          value = typeAdapter.value().newInstance().unmarshal(value);
        }
        else {
          value = converter.convertForDeserialization(in.readObject(), property.getPropertyType());
        }
        property.getWriteMethod().invoke(o, value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
      catch (ClassNotFoundException e) {
        throw e;
      }
      catch (IllegalAccessException e) {
        throw e;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Writes out the properties of the given instance of the jaxb class.
   *
   * @param o The instance.
   * @param out The output.
   */
  public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {
    for (int i = 0; i < externalizableProperties.length; i++) {
      PropertyDescriptor property = externalizableProperties[i];
      try {
        Object value = property.getReadMethod().invoke(o);
        XmlJavaTypeAdapter typeAdapter = typeAdapters[i];
        if (typeAdapter != null) {
          value = typeAdapter.value().newInstance().marshal(value);
        }
        out.writeObject(value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
      catch (IllegalAccessException e) {
        throw e;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * @throws UnsupportedOperationException
   */
  public List<Field> findOrderedFields(Class<?> clazz) {
    throw new UnsupportedOperationException();
  }

  private XmlJavaTypeAdapter findTypeAdapter(PropertyDescriptor jaxbProperty) {
    XmlJavaTypeAdapter adapterInfo = jaxbProperty.getReadMethod().getAnnotation(XmlJavaTypeAdapter.class);

    if ((adapterInfo == null) && (jaxbProperty.getWriteMethod() != null)) {
      adapterInfo = jaxbProperty.getWriteMethod().getAnnotation(XmlJavaTypeAdapter.class);
    }

    if (adapterInfo == null) {
      adapterInfo = jaxbProperty.getPropertyType().getAnnotation(XmlJavaTypeAdapter.class);
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

  private static Type findAdaptingType(Class<? extends XmlAdapter> adapterClass) {
    Type superClass = adapterClass.getGenericSuperclass();
    if ((superClass instanceof ParameterizedType) &&
      (((ParameterizedType) superClass).getRawType() instanceof Class) &&
      (XmlAdapter.class.isAssignableFrom((Class) ((ParameterizedType) superClass).getRawType()))) {
      return ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    if (XmlAdapter.class.isAssignableFrom(adapterClass.getSuperclass())) {
      return findAdaptingType((Class<? extends XmlAdapter>) adapterClass.getSuperclass());
    }

    throw new IllegalStateException("Unable to find the adapting type for xml adapter " + adapterClass.getName());
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
