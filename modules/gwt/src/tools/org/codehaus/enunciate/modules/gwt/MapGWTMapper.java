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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class MapGWTMapper implements GWTMapper<Map, Map> {

  private final Class<Map> mapType;
  private final Type keyType;
  private final Type valueType;
  private final XmlJavaTypeAdapter adapterInfo;

  public MapGWTMapper(Class<Map> mapType, Type keyType, Type valueType, XmlJavaTypeAdapter adapterInfo) {
    this.mapType = mapType;
    this.keyType = keyType;
    this.valueType = valueType;
    this.adapterInfo = adapterInfo;
  }

  public Map toGWT(Map jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Map map = getMapInstance(this.mapType);
    for (Object entry : jaxbObject.entrySet()) {
      Object jaxbKey = ((Map.Entry) entry).getKey();
      Object jaxbValue = ((Map.Entry) entry).getValue();
      Object gwtKey = GWTMapperIntrospector.getGWTMapper(jaxbKey == null ? null : jaxbKey.getClass(), this.keyType, this.adapterInfo, null).toGWT(jaxbKey, context);
      Object gwtValue = GWTMapperIntrospector.getGWTMapper(jaxbValue == null ? null : jaxbValue.getClass(), this.valueType, this.adapterInfo, null).toGWT(jaxbValue, context);
      map.put(gwtKey, gwtValue);
    }
    return map;
  }

  public Map toJAXB(Map gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    Map map = MapGWTMapper.getMapInstance(this.mapType);
    for (Object entry : gwtObject.entrySet()) {
      Object gwtKey = ((Map.Entry) entry).getKey();
      Object gwtValue = ((Map.Entry) entry).getValue();
      GWTMapper keyMapper = GWTMapperIntrospector.getGWTMapperForGWTObject(gwtKey);
      if (keyMapper == null) {
        keyMapper = GWTMapperIntrospector.getGWTMapper(this.keyType, this.adapterInfo, null);
      }
      GWTMapper valueMapper = GWTMapperIntrospector.getGWTMapperForGWTObject(gwtValue);
      if (valueMapper == null) {
        valueMapper = GWTMapperIntrospector.getGWTMapper(this.valueType, this.adapterInfo, null);
      }
      Object jaxbKey = keyMapper.toJAXB(gwtKey, context);
      Object jaxbValue = valueMapper.toJAXB(gwtValue, context);
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
