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

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;

/**
 * @author Ryan Heaton
 */
public class MapGWTMapper implements GWTMapper<Map, Map> {

  private final Class<Map> mapType;
  private final GWTMapper keyMapper;
  private final GWTMapper valueMapper;

  public MapGWTMapper(Class<Map> mapType, GWTMapper keyMapper, GWTMapper valueMapper) {
    this.mapType = mapType;
    this.keyMapper = keyMapper;
    this.valueMapper = valueMapper;
  }

  public Map toGWT(Map jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Map map = getMapType(this.mapType);
    for (Object key : jaxbObject.keySet()) {
      map.put(this.keyMapper.toGWT(key, context), this.valueMapper.toGWT(jaxbObject.get(key), context));
    }
    return map;
  }

  public Map toJAXB(Map gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    Map map = getMapType(this.mapType);
    for (Object key : gwtObject.keySet()) {
      map.put(this.keyMapper.toJAXB(key, context), this.valueMapper.toJAXB(gwtObject.get(key), context));
    }
    return map;
  }

  public static Map getMapType(Class<Map> mapType) {
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
