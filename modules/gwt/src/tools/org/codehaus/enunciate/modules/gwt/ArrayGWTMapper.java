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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.lang.reflect.Array;

/**
 * @author Ryan Heaton
 */
public class ArrayGWTMapper implements GWTMapper {

  private final Class itemClass;
  private final GWTMapper itemMapper;

  public ArrayGWTMapper(GWTMapper itemMapper) {
    this.itemMapper = itemMapper;
    this.itemClass = null;
  }

  public ArrayGWTMapper(GWTMapper itemMapper, Class itemClass) {
    this.itemMapper = itemMapper;
    this.itemClass = itemClass;
  }

  public Object toGWT(Object jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    if (!jaxbObject.getClass().isArray()) {
      throw new GWTMappingException("Expected an array, got " + jaxbObject);
    }

    int length = Array.getLength(jaxbObject);
    if (length == 0) {
      return null;
    }

    Object item = itemMapper.toGWT(Array.get(jaxbObject, 0), context);
    Object resultArray = Array.newInstance(this.itemClass == null ? item.getClass() : this.itemClass, length);
    Array.set(resultArray, 0, item);
    int i = 1;
    while (length > i) {
      item = itemMapper.toGWT(Array.get(jaxbObject, i), context);
      Array.set(resultArray, i++, item);
    }
    return resultArray;
  }

  public Object toJAXB(Object gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    if (!gwtObject.getClass().isArray()) {
      throw new GWTMappingException("Expected an array, got " + gwtObject);
    }

    int length = Array.getLength(gwtObject);
    if (length == 0) {
      return null;
    }

    Object item = itemMapper.toJAXB(Array.get(gwtObject, 0), context);
    Object resultArray = Array.newInstance(this.itemClass == null ? item.getClass() : this.itemClass, length);
    Array.set(resultArray, 0, item);
    int i = 1;
    while (length > i) {
      item = itemMapper.toJAXB(Array.get(gwtObject, i), context);
      Array.set(resultArray, i++, item);
    }
    return resultArray;
  }
}
