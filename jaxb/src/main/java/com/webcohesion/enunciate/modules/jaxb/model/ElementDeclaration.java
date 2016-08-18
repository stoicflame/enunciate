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

import javax.xml.namespace.QName;

/**
 * Common interface for an element declaration.
 * 
 * @author Ryan Heaton
 */
public interface ElementDeclaration extends javax.lang.model.element.Element {
  
  /**
   * The name of the xml element declaration.
   *
   * @return The name of the xml element declaration.
   */
  String getName();

  /**
   * The namespace of the xml element.
   *
   * @return The namespace of the xml element.
   */
  String getNamespace();

  /**
   * The qname for this root element.
   *
   * @return The qname for this root element.
   */
  QName getQname();

}
