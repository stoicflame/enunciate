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

  public static AMFMapper getAMFMapper(Type jaxbType, XmlJavaTypeAdapter adapterInfo) {
    if (jaxbType == null) {
      jaxbType = Object.class;
    }

    AMFMapper mapper;
    if (AMFMapperIntrospector.MAPPERS.containsKey(jaxbType)) {
      mapper = AMFMapperIntrospector.MAPPERS.get(jaxbType);
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
            mapper = new MapAMFMapper((Class<Map>) rawType, AMFMapperIntrospector.getAMFMapper(keyType, adapterInfo), AMFMapperIntrospector.getAMFMapper(valueType, adapterInfo));
          }
          else if (Collection.class.isAssignableFrom((Class) rawType)) {
            Type itemType = Object.class;
            Type[] typeArgs = ((ParameterizedType) jaxbType).getActualTypeArguments();
            if ((typeArgs != null) && (typeArgs.length == 1)) {
              itemType = typeArgs[0];
            }
            mapper = new CollectionAMFMapper((Class<Collection>) rawType, AMFMapperIntrospector.getAMFMapper(itemType, adapterInfo));
          }
          else {
            mapper = AMFMapperIntrospector.getAMFMapper(rawType, adapterInfo);
          }
        }
        else {
          mapper = AMFMapperIntrospector.getAMFMapper(rawType, adapterInfo);
        }
      }
      else if (jaxbType instanceof GenericArrayType) {
        mapper = new ArrayAMFMapper(AMFMapperIntrospector.getAMFMapper(((GenericArrayType) jaxbType).getGenericComponentType(), adapterInfo));
      }
      else if (jaxbType instanceof TypeVariable) {
        mapper = AMFMapperIntrospector.getAMFMapper(((TypeVariable) jaxbType).getBounds()[0], adapterInfo);
      }
      else if (jaxbType instanceof WildcardType) {
        mapper = AMFMapperIntrospector.getAMFMapper(((WildcardType) jaxbType).getUpperBounds()[0], adapterInfo);
      }
      else if (jaxbType instanceof Class) {
        Class jaxbClass = ((Class) jaxbType);
        if (jaxbClass.isArray()) {
          return jaxbClass.getComponentType().isPrimitive() ? DefaultAMFMapper.INSTANCE : new ArrayAMFMapper(AMFMapperIntrospector.getAMFMapper(jaxbClass.getComponentType(), adapterInfo));
        }
        else if (Collection.class.isAssignableFrom(jaxbClass)) {
          return new CollectionAMFMapper((Class<? extends Collection>) jaxbClass, DefaultAMFMapper.INSTANCE);
        }
        else if (Map.class.isAssignableFrom(jaxbClass)) {
          return new MapAMFMapper((Class<Map>) jaxbClass, DefaultAMFMapper.INSTANCE, DefaultAMFMapper.INSTANCE);
        }
        else {
          adapterInfo = adapterInfo == null ? (XmlJavaTypeAdapter) jaxbClass.getAnnotation(XmlJavaTypeAdapter.class) : adapterInfo;

          if (adapterInfo != null) {
            //if it's adapted, don't cache it.
            AMFMapper adaptingMapper = AMFMapperIntrospector.getAMFMapper(AMFMapperIntrospector.findAdaptingType(adapterInfo.value()), null);
            try {
              return new AdaptingAMFMapper(adapterInfo.value().newInstance(), adaptingMapper);
            }
            catch (Exception e) {
              throw new AMFMappingException(e);
            }
          }
          else if (Enum.class.isAssignableFrom(jaxbClass)) {
            mapper = new EnumAMFMapper(jaxbClass);
          }
          else if (jaxbClass.isPrimitive()) {
            mapper = DefaultAMFMapper.INSTANCE;
          }
          else {
            try {
              mapper = (AMFMapper) Class.forName(jaxbClass.getPackage().getName() + ".amf." + jaxbClass.getSimpleName() + "AMFMapper").newInstance();
            }
            catch (ClassNotFoundException e) {
              mapper = DefaultAMFMapper.INSTANCE;
            }
            catch (Exception e) {
              throw new AMFMappingException("Unable to instantiate class '" + jaxbClass.getPackage().getName() + ".amf." + jaxbClass.getSimpleName() + "AMFMapper'.", e);
            }
          }
        }
      }
      else {
        mapper = DefaultAMFMapper.INSTANCE;
      }

      AMFMapperIntrospector.MAPPERS.put(jaxbType, mapper);
    }


    return mapper;
  }

  private static Type findAdaptingType(Class<? extends XmlAdapter> adapterClass) {
    Type superClass = adapterClass.getGenericSuperclass();
    if ((superClass instanceof ParameterizedType) &&
      (((ParameterizedType) superClass).getRawType() instanceof Class) &&
      (XmlAdapter.class.isAssignableFrom((Class) ((ParameterizedType) superClass).getRawType()))) {
      return ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    if (XmlAdapter.class.isAssignableFrom(adapterClass.getSuperclass())) {
      return AMFMapperIntrospector.findAdaptingType((Class<? extends XmlAdapter>) adapterClass.getSuperclass());
    }

    throw new IllegalStateException("Unable to find the adapting type for xml adapter " + adapterClass.getName());
  }

  private AMFMapperIntrospector() {
  }

}
