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

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.*;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Introspector used to lookup AMF mappers.
 *
 * @author Ryan Heaton
 */
public class AMFMapperIntrospector {

  private static final Map<Type, AMFMapper> MAPPERS = new HashMap<Type, AMFMapper>();

  static {
    AMFMapperIntrospector.MAPPERS.put(DataHandler.class, new DataHandlerAMFMapper());
    AMFMapperIntrospector.MAPPERS.put(QName.class, new QNameAMFMapper());
    AMFMapperIntrospector.MAPPERS.put(URI.class, new URIAMFMapper());
    AMFMapperIntrospector.MAPPERS.put(UUID.class, new UUIDAMFMapper());
    AMFMapperIntrospector.MAPPERS.put(XMLGregorianCalendar.class, new XMLGregorianCalendarAMFMapper());
  }

  public static AMFMapper getAMFMapper(Type jaxbType) {
    return getAMFMapper(null, jaxbType);
  }

  public static AMFMapper getAMFMapper(Class realType, Type jaxbType) {
    return getAMFMapper(realType, jaxbType, null, null);
  }

  public static AMFMapper getAMFMapper(Type jaxbType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    return getAMFMapper(null, jaxbType, adapterInfo, elementInfo);
  }

  public static AMFMapper getAMFMapper(Class realType, Type jaxbType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    if ((realType != null) && (!realType.isArray()) && (!realType.isPrimitive()) && (realType.getPackage() != null)) {
      //first check the real type.  if a mapper exists, use it, otherwise use the type defined in the signature.
      if (MAPPERS.containsKey(realType)) {
        return MAPPERS.get(realType);
      }

      try {
        loadCustomMapperClass(realType);
        jaxbType = realType;
      }
      catch (Throwable e) {
        //fall through.
      }
    }

    if (jaxbType == null) {
      jaxbType = Object.class;
    }

    Class specifiedType = ((elementInfo != null) && (elementInfo.type() != null) && (elementInfo.type() != XmlElement.DEFAULT.class)) ? elementInfo.type() : null;
    AMFMapper mapper;
    if (MAPPERS.containsKey(jaxbType)) {
      mapper = MAPPERS.get(jaxbType);
    }
    else {
      if (jaxbType instanceof ParameterizedType) {
        Type rawType = ((ParameterizedType) jaxbType).getRawType();

        if (rawType instanceof Class) {
          if (Map.class.isAssignableFrom((Class) rawType)) {
            Type keyType = Object.class;
            Type valueType = Object.class;
            Type[] typeArgs = ((ParameterizedType) jaxbType).getActualTypeArguments();
            if ((typeArgs != null) && (typeArgs.length == 2)) {
              keyType = typeArgs[0];
              valueType = typeArgs[1];
            }
            mapper = new MapAMFMapper((Class<Map>) rawType, keyType, valueType, adapterInfo);
          }
          else if (Collection.class.isAssignableFrom((Class) rawType)) {
            Type itemType = Object.class;
            Type[] typeArgs = ((ParameterizedType) jaxbType).getActualTypeArguments();
            //the type can be specified via annotation in this case...
            if (specifiedType != null) {
              itemType = specifiedType;
            }
            else if ((typeArgs != null) && (typeArgs.length == 1)) {
              itemType = typeArgs[0];
            }
            mapper = new CollectionAMFMapper((Class<Collection>) rawType, itemType, adapterInfo, null);
          }
          else {
            mapper = getAMFMapper(rawType, adapterInfo, elementInfo);
          }
        }
        else {
          mapper = getAMFMapper(rawType, adapterInfo, elementInfo);
        }
      }
      else if (jaxbType instanceof GenericArrayType) {
        mapper = new ArrayAMFMapper(((GenericArrayType) jaxbType).getGenericComponentType(), adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof TypeVariable) {
        mapper = getAMFMapper(((TypeVariable) jaxbType).getBounds()[0], adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof WildcardType) {
        mapper = getAMFMapper(((WildcardType) jaxbType).getUpperBounds()[0], adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof Class) {
        Class jaxbClass = ((Class) jaxbType);
        if (jaxbClass.isArray()) {
          mapper = jaxbClass.getComponentType().isPrimitive() ? DefaultAMFMapper.INSTANCE : new ArrayAMFMapper(jaxbClass.getComponentType(), adapterInfo, elementInfo);
        }
        else if (Collection.class.isAssignableFrom(jaxbClass)) {
          if (specifiedType != null) {
            mapper = new CollectionAMFMapper((Class<? extends Collection>) jaxbClass, specifiedType, adapterInfo, null);
          }
          else {
            mapper = new CollectionAMFMapper((Class<? extends Collection>) jaxbClass, Object.class, null, null);
          }
        }
        else if (Map.class.isAssignableFrom(jaxbClass)) {
          mapper = new MapAMFMapper((Class<Map>) jaxbClass, Object.class, Object.class, null);
        }
        else {
          adapterInfo = adapterInfo == null ? (XmlJavaTypeAdapter) jaxbClass.getAnnotation(XmlJavaTypeAdapter.class) : adapterInfo;

          if (adapterInfo != null) {
            Type adaptingType = findAdaptingType(adapterInfo.value());
            AMFMapper adaptingMapper = getAMFMapper(adaptingType);
            try {
              //if it's adapted, don't cache it (return it directly).
              return new AdaptingAMFMapper(adapterInfo.value().newInstance(), adaptingMapper, jaxbClass, narrowType(adaptingType));
            }
            catch (Exception e) {
              throw new AMFMappingException(e);
            }
          }
          else if (specifiedType != null) {
            mapper = getAMFMapper(specifiedType);
          }
          else if (Enum.class.isAssignableFrom(jaxbClass)) {
            mapper = new EnumAMFMapper(jaxbClass);
          }
          else if (jaxbClass.isPrimitive()) {
            mapper = DefaultAMFMapper.INSTANCE;
          }
          else {
            try {
              mapper = loadCustomMapperClass(jaxbClass).newInstance();
            }
            catch (ClassNotFoundException e) {
              mapper = DefaultAMFMapper.INSTANCE;
            }
            catch (NoClassDefFoundError e) {
              mapper = DefaultAMFMapper.INSTANCE;
            }
            catch (Throwable e) {
              throw new AMFMappingException("Unable to instantiate class '" + jaxbClass.getPackage().getName() + ".amf." + jaxbClass.getSimpleName() + "AMFMapper'.", e);
            }
          }
        }
      }
      else {
        mapper = DefaultAMFMapper.INSTANCE;
      }

      if (specifiedType == null) { //only cache if the type isn't specified.
        MAPPERS.put(jaxbType, mapper);
      }
    }

    return mapper;
  }

  public static Class narrowType(Type type) {
    if (type instanceof Class) {
      return (Class) type;
    }
    else if (type instanceof ParameterizedType) {
      return narrowType(((ParameterizedType) type).getRawType());
    }
    else if (type instanceof GenericArrayType) {
      return Array.newInstance(narrowType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
    }
    else if (type instanceof TypeVariable) {
      return narrowType(((TypeVariable) type).getBounds()[0]);
    }
    else if (type instanceof WildcardType) {
      return narrowType(((WildcardType) type).getUpperBounds()[0]);
    }
    else {
      throw new IllegalArgumentException("Unsupported Type type: " + type);
    }
  }

  private static Class<? extends AMFMapper> loadCustomMapperClass(Class jaxbClass) throws ClassNotFoundException {
    return (Class<? extends AMFMapper>) Class.forName(jaxbClass.getPackage().getName() + ".amf." + jaxbClass.getSimpleName() + "AMFMapper");
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

  private AMFMapperIntrospector() {
  }

}
