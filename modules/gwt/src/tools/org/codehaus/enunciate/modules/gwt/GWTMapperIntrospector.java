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

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.lang.reflect.*;

/**
 * Introspector used to lookup GWT mappers.
 *
 * @author Ryan Heaton
 */
public class GWTMapperIntrospector {

  private static final Map<Type, GWTMapper> MAPPERS = new HashMap<Type, GWTMapper>();

  static {
    MAPPERS.put(BigDecimal.class, new BigDecimalGWTMapper());
    MAPPERS.put(BigInteger.class, new BigIntegerGWTMapper());
    MAPPERS.put(Calendar.class, new CalendarGWTMapper());
    MAPPERS.put(DataHandler.class, new DataHandlerGWTMapper());
    MAPPERS.put(QName.class, new QNameGWTMapper());
    MAPPERS.put(URI.class, new URIGWTMapper());
    MAPPERS.put(UUID.class, new UUIDGWTMapper());
    MAPPERS.put(XMLGregorianCalendar.class, new XMLGregorianCalendarGWTMapper());
  }

  public static GWTMapper getGWTMapper(Type jaxbType, XmlJavaTypeAdapter adapterInfo) {
    if (jaxbType == null) {
      jaxbType = Object.class;
    }
    
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
            mapper = new MapGWTMapper((Class<Map>) rawType, getGWTMapper(keyType, adapterInfo), getGWTMapper(valueType, adapterInfo));
          }
          else if (Collection.class.isAssignableFrom((Class) rawType)) {
            Type itemType = Object.class;
            Type[] typeArgs = ((ParameterizedType) jaxbType).getActualTypeArguments();
            if ((typeArgs != null) && (typeArgs.length == 1)) {
              itemType = typeArgs[0];
            }
            mapper = new CollectionGWTMapper((Class<Collection>) rawType, getGWTMapper(itemType, adapterInfo));
          }
          else {
            mapper = getGWTMapper(rawType, adapterInfo);
          }
        }
        else {
          mapper = getGWTMapper(rawType, adapterInfo);
        }
      }
      else if (jaxbType instanceof GenericArrayType) {
        mapper = new ArrayGWTMapper(getGWTMapper(((GenericArrayType) jaxbType).getGenericComponentType(), adapterInfo));
      }
      else if (jaxbType instanceof TypeVariable) {
        mapper = getGWTMapper(((TypeVariable) jaxbType).getBounds()[0], adapterInfo);
      }
      else if (jaxbType instanceof WildcardType) {
        mapper = getGWTMapper(((WildcardType) jaxbType).getUpperBounds()[0], adapterInfo);
      }
      else if (jaxbType instanceof Class) {
        Class jaxbClass = ((Class) jaxbType);
        if (jaxbClass.isArray()) {
          return jaxbClass.getComponentType().isPrimitive() ? DefaultGWTMapper.INSTANCE : new ArrayGWTMapper(getGWTMapper(jaxbClass.getComponentType(), adapterInfo));
        }
        else if (Collection.class.isAssignableFrom(jaxbClass)) {
          return new CollectionGWTMapper((Class<? extends Collection>) jaxbClass, DefaultGWTMapper.INSTANCE);
        }
        else if (Map.class.isAssignableFrom(jaxbClass)) {
          return new MapGWTMapper((Class<Map>) jaxbClass, DefaultGWTMapper.INSTANCE, DefaultGWTMapper.INSTANCE);
        }
        else {
          adapterInfo = adapterInfo == null ? (XmlJavaTypeAdapter) jaxbClass.getAnnotation(XmlJavaTypeAdapter.class) : null;

          if (adapterInfo != null) {
            //if it's adapted, don't cache it.
            GWTMapper adaptingMapper = getGWTMapper(findAdaptingType(adapterInfo.value()), null);
            try {
              return new AdaptingGWTMapper(adapterInfo.value().newInstance(), adaptingMapper);
            }
            catch (Exception e) {
              throw new GWTMappingException(e);
            }
          }
          else if (Enum.class.isAssignableFrom(jaxbClass)) {
            mapper = new EnumGWTMapper(jaxbClass);
          }
          else if (jaxbClass.isPrimitive()) {
            mapper = DefaultGWTMapper.INSTANCE;
          }
          else {
            try {
              mapper = (GWTMapper) Class.forName(jaxbClass.getPackage().getName() + ".gwt." + jaxbClass.getSimpleName() + "GWTMapper").newInstance();
            }
            catch (ClassNotFoundException e) {
              mapper = DefaultGWTMapper.INSTANCE;
            }
            catch (Exception e) {
              throw new GWTMappingException("Unable to instantiate class '" + jaxbClass.getPackage().getName() + ".gwt." + jaxbClass.getSimpleName() + "GWTMapper'.", e);
            }
          }
        }
      }
      else {
        mapper = DefaultGWTMapper.INSTANCE;
      }

      MAPPERS.put(jaxbType, mapper);
    }


    return mapper;
  }

  private static Type findAdaptingType(Class<? extends XmlAdapter> adapterClass) {
    for (Type superInterface : adapterClass.getGenericInterfaces()) {
      if ((superInterface instanceof ParameterizedType) &&
         (((ParameterizedType) superInterface).getRawType() instanceof Class) &&
         (XmlAdapter.class.isAssignableFrom((Class) ((ParameterizedType) superInterface).getRawType()))) {
        return ((ParameterizedType) superInterface).getActualTypeArguments()[0];
      }
    }

    if (XmlAdapter.class.isAssignableFrom(adapterClass.getSuperclass())) {
      return findAdaptingType((Class<? extends XmlAdapter>) adapterClass.getSuperclass());
    }

    throw new IllegalStateException("Unable to find the adapting type for xml adapter " + adapterClass.getName());
  }

  private GWTMapperIntrospector() {
  }

}
