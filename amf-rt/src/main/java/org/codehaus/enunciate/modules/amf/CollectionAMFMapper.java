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
import javax.xml.bind.annotation.XmlElement;
import java.util.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * @author Ryan Heaton
 */
public class CollectionAMFMapper implements AMFMapper<Collection, Collection> {

  private final Class<? extends Collection> collectionType;
  private final Type defaultItemType;
  private final XmlJavaTypeAdapter adapterInfo;
  private final XmlElement elementInfo;

  public CollectionAMFMapper(Class<? extends Collection> collectionType, Type defaultItemType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    this.collectionType = collectionType;
    this.defaultItemType = defaultItemType;
    this.adapterInfo = adapterInfo;
    this.elementInfo = elementInfo;
  }

  public Collection toAMF(Collection jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Collection collection = CollectionAMFMapper.newCollectionInstance(collectionType);
    for (Object item : jaxbObject) {
      Object toItem  = item != null ? AMFMapperIntrospector.getAMFMapper(item.getClass(), this.defaultItemType, this.adapterInfo, this.elementInfo).toAMF(item, context) : null;
      collection.add(toItem);
    }
    return collection;
  }

  public Collection toJAXB(Collection amfObject, AMFMappingContext context) throws AMFMappingException {
    if (amfObject == null) {
      return null;
    }

    Collection collection = CollectionAMFMapper.newCollectionInstance(collectionType);
    for (Object item : amfObject) {
      AMFMapper itemMapper = item instanceof AMFMapperAware ? ((AMFMapperAware) item).loadAMFMapper() : AMFMapperIntrospector.getAMFMapper(this.defaultItemType, this.adapterInfo, this.elementInfo);
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
