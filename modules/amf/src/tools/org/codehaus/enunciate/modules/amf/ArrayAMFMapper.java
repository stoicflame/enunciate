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

import java.lang.reflect.Array;

/**
 * @author Ryan Heaton
 */
public class ArrayAMFMapper implements AMFMapper {

  private final Class itemClass;
  private final AMFMapper itemMapper;

  public ArrayAMFMapper(AMFMapper itemMapper) {
    this.itemMapper = itemMapper;
    this.itemClass = null;
  }

  public ArrayAMFMapper(AMFMapper itemMapper, Class itemClass) {
    this.itemMapper = itemMapper;
    this.itemClass = itemClass;
  }

  public Object toAMF(Object jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

    if (!jaxbObject.getClass().isArray()) {
      throw new AMFMappingException("Expected an array, got " + jaxbObject);
    }

    int length = Array.getLength(jaxbObject);
    if (length == 0) {
      return null;
    }

    Object item = itemMapper.toAMF(Array.get(jaxbObject, 0), context);
    Object resultArray = Array.newInstance(this.itemClass == null ? item.getClass() : this.itemClass, length);
    Array.set(resultArray, 0, item);
    int i = 1;
    while (length > i) {
      item = itemMapper.toAMF(Array.get(jaxbObject, i), context);
      Array.set(resultArray, i++, item);
    }
    return resultArray;
  }

  public Object toJAXB(Object amfObject, AMFMappingContext context) throws AMFMappingException {
    if (amfObject == null) {
      return null;
    }

    if (!amfObject.getClass().isArray()) {
      throw new AMFMappingException("Expected an array, got " + amfObject);
    }

    int length = Array.getLength(amfObject);
    if (length == 0) {
      return null;
    }

    Object item = itemMapper.toJAXB(Array.get(amfObject, 0), context);
    Object resultArray = Array.newInstance(this.itemClass == null ? item.getClass() : this.itemClass, length);
    Array.set(resultArray, 0, item);
    int i = 1;
    while (length > i) {
      item = itemMapper.toJAXB(Array.get(amfObject, i), context);
      Array.set(resultArray, i++, item);
    }
    return resultArray;
  }
}
