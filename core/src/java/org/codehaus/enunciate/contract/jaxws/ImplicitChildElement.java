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

package org.codehaus.enunciate.contract.jaxws;

import org.codehaus.enunciate.contract.jaxb.types.XmlType;

import javax.xml.namespace.QName;

import com.sun.mirror.type.TypeMirror;

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
   * Gets the java type for this child element.
   *
   * @return The xml type.
   */
  TypeMirror getType();
}
