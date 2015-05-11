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

package com.webcohesion.enunciate.modules.jackson1.model;


import com.webcohesion.enunciate.modules.jackson1.model.types.XmlType;

import javax.lang.model.type.TypeMirror;

/**
 * An implicit child element reference.
 *
 * @author Ryan Heaton
 */
public class ImplicitChildElementRef extends ImplicitElementRef implements ImplicitChildElement {

  public ImplicitChildElementRef(Element element) {
    super(element);
  }

  @Override
  public int getMinOccurs() {
    return 0;
  }

  @Override
  public String getMaxOccurs() {
    return "unbounded";
  }

  @Override
  public XmlType getXmlType() {
    return element.getBaseType();
  }

  @Override
  public TypeMirror getType() {
    return element.getCollectionItemType();
  }

  @Override
  public String getMimeType() {
    return element.getMimeType();
  }

  @Override
  public boolean isSwaRef() {
    return element.isSwaRef();
  }

}