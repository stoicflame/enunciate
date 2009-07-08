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

import com.sun.mirror.type.PrimitiveType;
import net.sf.jelly.apt.decorations.type.DecoratedPrimitiveType;

import javax.xml.namespace.QName;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.enunciate.util.WhateverNode;
import org.codehaus.enunciate.util.RawValueNode;

/**
 * @author Ryan Heaton
 */
public class XmlPrimitiveType extends DecoratedPrimitiveType implements XmlType {

  public XmlPrimitiveType(PrimitiveType delegate) {
    super(delegate);
  }

  public String getName() {
    switch (getKind()) {
      case BOOLEAN:
        return "boolean";
      case BYTE:
        return "byte";
      case DOUBLE:
        return "double";
      case FLOAT:
        return "float";
      case INT:
        return "int";
      case LONG:
        return "long";
      case SHORT:
        return "short";
    }

    return null;
  }

  public String getNamespace() {
    return "http://www.w3.org/2001/XMLSchema";
  }

  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  public boolean isAnonymous() {
    return false;
  }

  public boolean isSimple() {
    return true;
  }

  public void generateExampleXml(org.jdom.Element node, String specifiedValue) {
    node.addContent(new org.jdom.Text(specifiedValue == null ? "..." : specifiedValue));
  }

  public JsonNode generateExampleJson(String specifiedValue) {
    switch (getKind()) {
      case BOOLEAN:
        return JsonNodeFactory.instance.booleanNode("true".equalsIgnoreCase(specifiedValue));
      case BYTE:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
        return specifiedValue == null ? WhateverNode.instance : new RawValueNode(specifiedValue);
      default:
        return specifiedValue == null ? WhateverNode.instance : JsonNodeFactory.instance.textNode(specifiedValue);
    }
  }
}
