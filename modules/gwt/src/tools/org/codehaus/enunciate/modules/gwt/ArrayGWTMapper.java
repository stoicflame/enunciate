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
public class ArrayGWTMapper implements GWTMapper<Object[], Object[]> {

  private final CollectionGWTMapper collectionMapper;

  public ArrayGWTMapper(GWTMapper itemMapper) {
    this.collectionMapper = new CollectionGWTMapper(ArrayList.class, itemMapper);
  }

  public Object[] toGWT(Object[] jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Collection gwtCollection = this.collectionMapper.toGWT(Arrays.asList(jaxbObject), context);
    if (gwtCollection.isEmpty()) {
      return null;
    }

    return gwtCollection.toArray((Object[]) Array.newInstance(gwtCollection.iterator().next().getClass(), gwtCollection.size()));
  }

  public Object[] toJAXB(Object[] gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }
    Collection jaxbCollection = this.collectionMapper.toJAXB(Arrays.asList(gwtObject), context);
    if (jaxbCollection.isEmpty()) {
      return null;
    }
    return jaxbCollection.toArray((Object[]) Array.newInstance(jaxbCollection.iterator().next().getClass(), jaxbCollection.size()));
  }
}
