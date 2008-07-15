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

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

/**
 * Introspector used to lookup GWT mappers.
 *
 * @author Ryan Heaton
 */
public class GWTMapperIntrospector {

  private static final Map<Type, GWTMapper> MAPPERS = new HashMap<Type, GWTMapper>();
  private static final Properties GWT2JAXBMAPPINGS = new Properties();

  static {
    MAPPERS.put(BigDecimal.class, new BigDecimalGWTMapper());
    MAPPERS.put(BigInteger.class, new BigIntegerGWTMapper());
    MAPPERS.put(Calendar.class, new CalendarGWTMapper());
    MAPPERS.put(DataHandler.class, new DataHandlerGWTMapper());
    MAPPERS.put(QName.class, new QNameGWTMapper());
    MAPPERS.put(URI.class, new URIGWTMapper());
    MAPPERS.put(UUID.class, new UUIDGWTMapper());
    MAPPERS.put(XMLGregorianCalendar.class, new XMLGregorianCalendarGWTMapper());

    try {
      InputStream mappings = GWTMapperIntrospector.class.getResourceAsStream("/gwt-to-jaxb-mappings.properties");
      if (mappings != null) {
        GWT2JAXBMAPPINGS.load(mappings);
      }
    }
    catch (Exception e) {
      //fall through... forget the mappings load.
    }
  }

  public static GWTMapper getGWTMapper(Type jaxbType) {
    return getGWTMapper(null, jaxbType);
  }

  public static GWTMapper getGWTMapper(Class realType, Type jaxbType) {
    return getGWTMapper(realType, jaxbType, null, null);
  }

  public static GWTMapper getGWTMapper(Type jaxbType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    return getGWTMapper(null, jaxbType, adapterInfo, elementInfo);
  }

  public static GWTMapper getGWTMapperForGWTObject(Object gwtObject) {
    GWTMapper result = null;
    if (gwtObject != null) {
      Class gwtType = gwtObject.getClass();
      if ((gwtType != null) && (!gwtType.isArray()) && (!gwtType.isPrimitive())) {
        String jaxbType = GWT2JAXBMAPPINGS.getProperty(gwtType.getName());
        if (jaxbType != null) {
          try {
            result = getGWTMapper(Class.forName(jaxbType));
          }
          catch (Throwable e) {
            result = null;
          }
        }
      }
    }
    return result;
  }

  public static GWTMapper getGWTMapper(Class realType, Type jaxbType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
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
    GWTMapper mapper;
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
            mapper = new MapGWTMapper((Class<Map>) rawType, keyType, valueType, adapterInfo);
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
            mapper = new CollectionGWTMapper((Class<Collection>) rawType, itemType, adapterInfo, null);
          }
          else {
            mapper = getGWTMapper(rawType, adapterInfo, elementInfo);
          }
        }
        else {
          mapper = getGWTMapper(rawType, adapterInfo, elementInfo);
        }
      }
      else if (jaxbType instanceof GenericArrayType) {
        mapper = new ArrayGWTMapper(((GenericArrayType) jaxbType).getGenericComponentType(), adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof TypeVariable) {
        mapper = getGWTMapper(((TypeVariable) jaxbType).getBounds()[0], adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof WildcardType) {
        mapper = getGWTMapper(((WildcardType) jaxbType).getUpperBounds()[0], adapterInfo, elementInfo);
      }
      else if (jaxbType instanceof Class) {
        Class jaxbClass = ((Class) jaxbType);
        if (jaxbClass.isArray()) {
          mapper = jaxbClass.getComponentType().isPrimitive() ? DefaultGWTMapper.INSTANCE : new ArrayGWTMapper(jaxbClass.getComponentType(), adapterInfo, elementInfo);
        }
        else if (Collection.class.isAssignableFrom(jaxbClass)) {
          if (specifiedType != null) {
            mapper = new CollectionGWTMapper((Class<? extends Collection>) jaxbClass, specifiedType, adapterInfo, null);
          }
          else {
            mapper = new CollectionGWTMapper((Class<? extends Collection>) jaxbClass, Object.class, null, null);
          }
        }
        else if (Map.class.isAssignableFrom(jaxbClass)) {
          mapper = new MapGWTMapper((Class<Map>) jaxbClass, Object.class, Object.class, null);
        }
        else {
          adapterInfo = adapterInfo == null ? (XmlJavaTypeAdapter) jaxbClass.getAnnotation(XmlJavaTypeAdapter.class) : adapterInfo;

          if (adapterInfo != null) {
            Type adaptingType = findAdaptingType(adapterInfo.value());
            GWTMapper adaptingMapper = getGWTMapper(adaptingType);
            try {
              //if it's adapted, don't cache it (return it directly).
              return new AdaptingGWTMapper(adapterInfo.value().newInstance(), adaptingMapper, jaxbClass, narrowType(adaptingType));
            }
            catch (Exception e) {
              throw new GWTMappingException(e);
            }
          }
          else if (specifiedType != null) {
            mapper = getGWTMapper(specifiedType);
          }
          else if (Enum.class.isAssignableFrom(jaxbClass)) {
            mapper = new EnumGWTMapper(jaxbClass);
          }
          else if (jaxbClass.isPrimitive()) {
            mapper = DefaultGWTMapper.INSTANCE;
          }
          else {
            try {
              mapper = loadCustomMapperClass(jaxbClass).newInstance();
            }
            catch (ClassNotFoundException e) {
              mapper = DefaultGWTMapper.INSTANCE;
            }
            catch (NoClassDefFoundError e) {
              mapper = DefaultGWTMapper.INSTANCE;
            }
            catch (Throwable e) {
              throw new GWTMappingException("Unable to instantiate class '" + jaxbClass.getPackage().getName() + ".gwt." + jaxbClass.getSimpleName() + "GWTMapper'.", e);
            }
          }
        }
      }
      else {
        mapper = DefaultGWTMapper.INSTANCE;
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

  private static Class<? extends GWTMapper> loadCustomMapperClass(Class jaxbClass) throws ClassNotFoundException {
    return (Class<? extends GWTMapper>) Class.forName(jaxbClass.getPackage().getName() + ".gwt." + jaxbClass.getSimpleName() + "GWTMapper");
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

  private GWTMapperIntrospector() {
  }

}
