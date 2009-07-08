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

package org.codehaus.enunciate.contract.jaxb.types;

import org.codehaus.jackson.JsonNode;

import javax.xml.namespace.QName;

/**
 * Type mirror that provides its qname.
 *
 * @author Ryan Heaton
 */
public interface XmlType {

  /**
   * The (local) name of this xml type.
   *
   * @return The (local) name of this xml type.
   */
  String getName();

  /**
   * The namespace for this xml type.
   *
   * @return The namespace for this xml type.
   */
  String getNamespace();

  /**
   * The qname of the xml type mirror.
   *
   * @return The qname of the xml type mirror.
   */
  QName getQname();

  /**
   * Whether this type is anonymous.
   *
   * @return Whether this type is anonymous.
   */
  boolean isAnonymous();

  /**
   * Whether this is a simple XML type.
   *
   * @return Whether this is a simple XML type.
   */
  boolean isSimple();

  /**
   * Generate some example xml to the given node.
   *
   * @param node The node.
   * @param specifiedValue A
   */
  void generateExampleXml(org.jdom.Element node, String specifiedValue);

  /**
   * Generate some example JSON for this type.
   *
   * @param specifiedValue The specified value, or null if none supplied.
   * @return The example JSON.
   */
  JsonNode generateExampleJson(String specifiedValue);
}
