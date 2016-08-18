/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxb.model;

import javax.lang.model.type.TypeMirror;
import javax.xml.namespace.QName;

import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;

/**
 * An implicit child element.
 *
 * @author Ryan Heaton
 */
public interface ImplicitChildElement extends ImplicitSchemaElement {

  /**
   * The value for the min occurs of the child element.
   *
   * @return The value for the min occurs of the child element.
   */
  public int getMinOccurs();

  /**
   * The value for the max occurs of the child element.
   *
   * @return The value for the max occurs of the child element.
   */
  public String getMaxOccurs();

  /**
   * The qname of the type for this element.  Since child element types cannot be anonymous, this value must not be null.
   *
   * @return The qname of the type for this element.
   */
  QName getTypeQName();

  /**
   * Gets the xml type for this child element.
   *
   * @return The xml type.
   */
  XmlType getXmlType();

  /**
   * The mime type for this child element (for binary data), or null if not applicable.
   *
   * @return The mime type or null.
   */
  String getMimeType();

  /**
   * Whether this child element is an attachment ref.
   *
   * @return Whether this child element is an attachment ref.
   */
  boolean isSwaRef();

  /**
   * Gets the java type for this child element.
   *
   * @return The xml type.
   */
  TypeMirror getType();
}
