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

import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.jackson.JsonNode;

import javax.xml.namespace.QName;

/**
 * Decorator for an xml class type.
 *
 * @author Ryan Heaton
 */
public class XmlClassType implements XmlType {

  private final TypeDefinition typeDef;

  public XmlClassType(TypeDefinition typeDef) {
    if (typeDef == null) {
      throw new IllegalArgumentException("A type definition must be supplied.");
    }

    this.typeDef = typeDef;
  }

  /**
   * The name of a class type depends on its type definition.
   *
   * @return The name of a class type depends on its type definition.
   */
  public String getName() {
    return this.typeDef.getName();
  }

  /**
   * The namespace of a class type depends on its type definition.
   *
   * @return The namespace of a class type depends on its type definition.
   */
  public String getNamespace() {
    return this.typeDef.getNamespace();
  }

  /**
   * The qname.
   *
   * @return The qname.
   */
  public QName getQname() {
    String localPart = getName();
    if (localPart == null) {
      localPart = "";
    }
    return new QName(getNamespace(), localPart);
  }

  /**
   * Whether a class type is anonymous depends on its type definition.
   *
   * @return Whether this class type is anonymous.
   */
  public boolean isAnonymous() {
    return this.typeDef.isAnonymous();
  }

  /**
   * This type is simple if its type definition is simple.
   *
   * @return This type is simple if its type definition is simple.
   */
  public boolean isSimple() {
    return this.typeDef.isSimple() || this.typeDef.isEnum();
  }

  /**
   * Get the type definition for this class type.
   *
   * @return The type definition for this class type.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDef;
  }

  // Inherited.
  public void generateExampleXml(org.jdom.Element node, String specifiedValue) {
    this.typeDef.generateExampleXml(node);
  }

  // Inherited.
  public JsonNode generateExampleJson(String specifiedValue) {
    return this.typeDef.generateExampleJson();
  }
}
