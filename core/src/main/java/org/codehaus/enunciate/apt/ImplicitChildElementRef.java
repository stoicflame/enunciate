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

package org.codehaus.enunciate.apt;

import com.sun.mirror.type.TypeMirror;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;

/**
 * An implicit child element reference.
 *
 * @author Ryan Heaton
 */
public class ImplicitChildElementRef extends ImplicitElementRef implements ImplicitChildElement {

  public ImplicitChildElementRef(Element element) {
    super(element);
  }

  public int getMinOccurs() {
    return 0;
  }

  public String getMaxOccurs() {
    return "unbounded";
  }

  public XmlType getXmlType() {
    return element.getBaseType();
  }

  public TypeMirror getType() {
    return element.getCollectionItemType();
  }

  public String getMimeType() {
    return element.getMimeType();
  }

  public boolean isSwaRef() {
    return element.isSwaRef();
  }
}