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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * @author Ryan Heaton
 */
public class ArrayAMFMapper implements CustomAMFMapper {
  
  private final Type declaredComponentType;
  private final Class jaxbItemClass;
  private final Class amfItemClass;
  private final XmlJavaTypeAdapter adapterInfo;
  private final XmlElement elementInfo;

  public ArrayAMFMapper(Type declaredComponentType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    this.declaredComponentType = declaredComponentType;
    this.adapterInfo = adapterInfo;
    this.elementInfo = elementInfo;
    this.jaxbItemClass = AMFMapperIntrospector.narrowType(this.declaredComponentType);
    AMFMapper defaultMapper = AMFMapperIntrospector.getAMFMapper(this.declaredComponentType, adapterInfo, elementInfo);
    if (defaultMapper instanceof CustomAMFMapper) {
      this.amfItemClass = ((CustomAMFMapper) defaultMapper).getAmfClass();
    }
    else {
      this.amfItemClass = this.jaxbItemClass;
    }
  }

  public Object toAMF(Object jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Object[] jaxbArray = (Object[]) jaxbObject;
    Object[] amfArray = (Object[]) Array.newInstance(this.amfItemClass, jaxbArray.length);
    for (int i = 0; i < jaxbArray.length; i++) {
      Object jaxbItem = jaxbArray[i];
      Object amfItem;
      if (jaxbItem == null) {
        amfItem = null;
      }
      else {
        AMFMapper itemMapper = AMFMapperIntrospector.getAMFMapper(jaxbItem.getClass(), this.declaredComponentType, this.adapterInfo, this.elementInfo);
        amfItem = itemMapper.toAMF(jaxbItem, context);
      }

      amfArray[i] = amfItem;
    }

    return amfArray;
  }

  public Object toJAXB(Object amfObject, AMFMappingContext context) throws AMFMappingException {
    if (amfObject == null) {
      return null;
    }

    Object[] amfArray = (Object[]) amfObject;
    Object[] jaxbArray = (Object[]) Array.newInstance(this.jaxbItemClass, amfArray.length);
    for (int i = 0; i < amfArray.length; i++) {
      Object amfItem = amfArray[i];
      Object jaxbItem;
      if (amfItem == null) {
        jaxbItem = null;
      }
      else {
        AMFMapper itemMapper = amfItem instanceof AMFMapperAware ? ((AMFMapperAware)amfItem).loadAMFMapper() : AMFMapperIntrospector.getAMFMapper(this.declaredComponentType, this.adapterInfo, this.elementInfo);
        jaxbItem = itemMapper.toJAXB(amfItem, context);
      }

      jaxbArray[i] = jaxbItem;
    }

    return jaxbArray;
  }

  public Class getJaxbClass() {
    return Array.newInstance(this.jaxbItemClass, 0).getClass();
  }

  public Class getAmfClass() {
    return Array.newInstance(this.amfItemClass, 0).getClass();
  }
}
