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

import org.jdom.Comment;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.enunciate.util.WhateverNode;

import javax.xml.namespace.QName;

/**
 * The marker xml type for a map.
 *
 * @author Ryan Heaton
 */
public class MapXmlType implements XmlType {

  private final XmlType keyType;
  private final XmlType valueType;

  public MapXmlType(XmlType keyType, XmlType valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /**
   * @return null (anonymous)
   */
  public String getName() {
    return null;
  }

  /**
   * @return null (default namespace)
   */
  public String getNamespace() {
    return null;
  }

  /**
   * @return null (anonymous)
   */
  public QName getQname() {
    return null;
  }

  /**
   * @return true
   */
  public boolean isAnonymous() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isSimple() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isMap() {
    return true;
  }

  /**
   * The xml type of the key for the map.
   *
   * @return The xml type of the key for the map.
   */
  public XmlType getKeyType() {
    return keyType;
  }

  /**
   * The xml type of the value for the map.
   *
   * @return The xml type of the value for the map.
   */
  public XmlType getValueType() {
    return valueType;
  }

  // Inherited.
  public void generateExampleXml(org.jdom.Element node, String specifiedValue) {
    for (int i = 0; i < 2; i++) {
      org.jdom.Element entry = new org.jdom.Element("entry", node.getNamespacePrefix(), node.getNamespaceURI());
      org.jdom.Element key = new org.jdom.Element("key", node.getNamespacePrefix(), node.getNamespaceURI());
      org.jdom.Element value = new org.jdom.Element("value", node.getNamespacePrefix(), node.getNamespaceURI());
      if (i == 0) {
        key.addContent(new Comment("type '" + this.keyType.getName() + "'"));
        this.keyType.generateExampleXml(key, null);
      }
      else {
        key.addContent(new Comment("(another '" + this.keyType.getName() + "' type)"));
      }
      entry.addContent(key);
      if (i == 0) {
        value.addContent(new Comment("content of type '" + this.valueType.getName() + "'"));
        this.valueType.generateExampleXml(value, null);
      }
      else {
        value.addContent(new Comment("(another '" + this.valueType.getName() + "' type)"));
      }
      entry.addContent(value);
      node.addContent(entry);
    }
    node.addContent(new org.jdom.Comment("...more entries..."));
  }

  public JsonNode generateExampleJson(String specifiedValue) {
    ArrayNode jsonNode = JsonNodeFactory.instance.arrayNode();
    for (int i = 0; i < 2; i++) {
      ObjectNode entryNode = JsonNodeFactory.instance.objectNode();
      if (i == 0) {
        entryNode.put("...", this.valueType.generateExampleJson(null));
        entryNode.put("...", WhateverNode.instance);
      }
      jsonNode.add(entryNode);
    }
    return jsonNode;
  }
}
