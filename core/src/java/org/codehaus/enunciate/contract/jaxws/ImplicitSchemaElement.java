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

package org.codehaus.enunciate.contract.jaxws;

import javax.xml.namespace.QName;

/**
 * An implicit schema element.  Implied by "literal" SOAP use.
 *
 * @author Ryan Heaton
 */
public interface ImplicitSchemaElement {

  /**
   * The local element name.
   *
   * @return The local element name.
   */
  String getElementName();

  /**
   * Documentation for the element, if it exists.
   *
   * @return Documentation for the element, or null if none.
   */
  String getElementDocs();

  /**
   * The qname of the type for this element, if the type is not anonymous.
   *
   * @return The qname of the type for this element, or null if it's an anonymous type.
   */
  QName getTypeQName();

}
