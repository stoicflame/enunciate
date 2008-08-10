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

package org.codehaus.enunciate.modules.gwt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * @author Ryan Heaton
 */
public class ArrayGWTMapper implements CustomGWTMapper {

  private final Type declaredComponentType;
  private final Class jaxbItemClass;
  private final Class gwtItemClass;
  private final XmlJavaTypeAdapter adapterInfo;
  private final XmlElement elementInfo;

  public ArrayGWTMapper(Type declaredComponentType, XmlJavaTypeAdapter adapterInfo, XmlElement elementInfo) {
    this.declaredComponentType = declaredComponentType;
    this.adapterInfo = adapterInfo;
    this.elementInfo = elementInfo;
    this.jaxbItemClass = GWTMapperIntrospector.narrowType(this.declaredComponentType);
    GWTMapper defaultMapper = GWTMapperIntrospector.getGWTMapper(this.declaredComponentType, adapterInfo, elementInfo);
    if (defaultMapper instanceof CustomGWTMapper) {
      this.gwtItemClass = ((CustomGWTMapper) defaultMapper).getGwtClass();
    }
    else {
      this.gwtItemClass = this.jaxbItemClass;
    }
  }

  public Object toGWT(Object jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    Object[] jaxbArray = (Object[]) jaxbObject;
    Object[] gwtArray = (Object[]) Array.newInstance(this.gwtItemClass, jaxbArray.length);
    for (int i = 0; i < jaxbArray.length; i++) {
      Object jaxbItem = jaxbArray[i];
      Object gwtItem;
      if (jaxbItem == null) {
        gwtItem = null;
      }
      else {
        GWTMapper itemMapper = GWTMapperIntrospector.getGWTMapper(jaxbItem.getClass(), this.declaredComponentType, this.adapterInfo, this.elementInfo);
        gwtItem = itemMapper.toGWT(jaxbItem, context);
      }

      gwtArray[i] = gwtItem;
    }

    return gwtArray;
  }

  public Object toJAXB(Object gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    Object[] gwtArray = (Object[]) gwtObject;
    Object[] jaxbArray = (Object[]) Array.newInstance(this.jaxbItemClass, gwtArray.length);
    for (int i = 0; i < gwtArray.length; i++) {
      Object gwtItem = gwtArray[i];
      Object jaxbItem;
      if (gwtItem == null) {
        jaxbItem = null;
      }
      else {
        GWTMapper itemMapper = GWTMapperIntrospector.getGWTMapperForGWTObject(gwtItem);
        if (itemMapper == null) {
          itemMapper = GWTMapperIntrospector.getGWTMapper(this.declaredComponentType, this.adapterInfo, this.elementInfo);
        }
        jaxbItem = itemMapper.toJAXB(gwtItem, context);
      }

      jaxbArray[i] = jaxbItem;
    }

    return jaxbArray;
  }

  public Class getJaxbClass() {
    return Array.newInstance(this.jaxbItemClass, 0).getClass();
  }

  public Class getGwtClass() {
    return Array.newInstance(this.gwtItemClass, 0).getClass();
  }
}
