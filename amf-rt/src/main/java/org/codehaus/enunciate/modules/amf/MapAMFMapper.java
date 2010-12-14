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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class MapAMFMapper implements AMFMapper<Map, Map> {

  private final Class<Map> mapType;
  private final Type keyType;
  private final Type valueType;
  private final XmlJavaTypeAdapter adapterInfo;

  public MapAMFMapper(Class<Map> mapType, Type keyType, Type valueType, XmlJavaTypeAdapter adapterInfo) {
    this.mapType = mapType;
    this.keyType = keyType;
    this.valueType = valueType;
    this.adapterInfo = adapterInfo;
  }

  public Map toAMF(Map jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Map map = getMapInstance(this.mapType);
    for (Object entry : jaxbObject.entrySet()) {
      Object jaxbKey = ((Map.Entry) entry).getKey();
      Object jaxbValue = ((Map.Entry) entry).getValue();
      Object amfKey = jaxbKey != null ? AMFMapperIntrospector.getAMFMapper(jaxbKey.getClass(), this.keyType, this.adapterInfo, null).toAMF(jaxbKey, context) : null;
      Object amfValue = jaxbValue != null ? AMFMapperIntrospector.getAMFMapper(jaxbValue.getClass(), this.valueType, this.adapterInfo, null).toAMF(jaxbValue, context) : null;
      map.put(amfKey, amfValue);
    }
    return map;
  }

  public Map toJAXB(Map amfObject, AMFMappingContext context) throws AMFMappingException {
    if (amfObject == null) {
      return null;
    }

    Map map = MapAMFMapper.getMapInstance(this.mapType);
    for (Object entry : amfObject.entrySet()) {
      Object amfKey = ((Map.Entry) entry).getKey();
      Object amfValue = ((Map.Entry) entry).getValue();
      AMFMapper keyMapper = amfKey instanceof AMFMapperAware ? ((AMFMapperAware) amfKey).loadAMFMapper() : AMFMapperIntrospector.getAMFMapper(this.keyType, this.adapterInfo, null);
      AMFMapper valueMapper = amfValue instanceof AMFMapperAware ? ((AMFMapperAware) amfValue).loadAMFMapper() : AMFMapperIntrospector.getAMFMapper(this.valueType, this.adapterInfo, null);
      Object jaxbKey = keyMapper.toJAXB(amfKey, context);
      Object jaxbValue = valueMapper.toJAXB(amfValue, context);
      map.put(jaxbKey, jaxbValue);
    }
    return map;
  }

  public static Map getMapInstance(Class<Map> mapType) {
    Map map;
    if ((mapType.isInterface()) || (Modifier.isAbstract(mapType.getModifiers()))) {
      map = new HashMap();
    }
    else {
      try {
        map = mapType.newInstance();
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Unable to create an instance of " + mapType.getName() + ".", e);
      }
    }
    return map;
  }
}
