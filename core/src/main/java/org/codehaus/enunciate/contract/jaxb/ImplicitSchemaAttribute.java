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

package org.codehaus.enunciate.contract.jaxb;

import javax.xml.namespace.QName;

/**
 * An implicit schema attribute.
 *
 * @author Ryan Heaton
 */
public interface ImplicitSchemaAttribute {

  /**
   * The local attribute name.
   *
   * @return The local attribute name.
   */
  String getAttributeName();

  /**
   * Documentation for the attribute, if it exists.
   *
   * @return Documentation for the attribute, or null if none.
   */
  String getAttributeDocs();

  /**
   * The qname of the type for this attribute, if the type is not anonymous.
   *
   * @return The qname of the type for this attribute, or null if it's an anonymous type.
   */
  QName getTypeQName();

}