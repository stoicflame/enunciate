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

import java.util.*;
import java.lang.reflect.Modifier;

/**
 * @author Ryan Heaton
 */
public class CollectionGWTMapper implements GWTMapper<Collection, Collection> {

  private final Class<? extends Collection> collectionType;
  private final GWTMapper itemMapper;

  public CollectionGWTMapper(Class<? extends Collection> collectionType, GWTMapper itemMapper) {
    this.collectionType = collectionType;
    this.itemMapper = itemMapper;
  }

  public Collection toGWT(Collection jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Collection collection = newCollectionInstance(collectionType);
    for (Object item : jaxbObject) {
      collection.add(itemMapper.toGWT(item, context));
    }
    return collection;
  }

  public Collection toJAXB(Collection gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    Collection collection = newCollectionInstance(collectionType);
    for (Object item : gwtObject) {
      collection.add(itemMapper.toJAXB(item, context));
    }
    return collection;
  }

  /**
   * Create a new instance of something of the specified collection type.
   *
   * @param collectionType The collection type.
   * @return the new instance.
   */
  public static Collection newCollectionInstance(Class collectionType) {
    if (Collection.class.isAssignableFrom(collectionType)) {
      Collection collection;
      if ((collectionType.isInterface()) || (Modifier.isAbstract(collectionType.getModifiers()))) {
        if (Set.class.isAssignableFrom(collectionType)) {
          if (SortedSet.class.isAssignableFrom(collectionType)) {
            collection = new TreeSet();
          }
          else {
            collection = new HashSet();
          }
        }
        else {
          collection = new ArrayList();
        }
      }
      else {
        try {
          collection = (Collection) collectionType.newInstance();
        }
        catch (Exception e) {
          throw new IllegalArgumentException("Unable to create an instance of " + collectionType.getName() + ".", e);
        }
      }
      return collection;
    }
    else {
      throw new IllegalArgumentException("Invalid list type: " + collectionType.getName());
    }
  }

}
