/**
 * Copyright 2011 Intellectual Reserve, Inc.
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
package org.codehaus.enunciate.modules.docs;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.types.*;
import org.codehaus.enunciate.doc.DocumentationExample;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Ryan Heaton
 */
public class GenerateExampleJsonMethod implements TemplateMethodModelEx {

  /**
   * Stack used for maintaining the list of type definitions for which we are currently generating example xml/json. Used to
   * prevent infinite recursion for circular references.
   */
  private static final ThreadLocal<Stack<String>> TYPE_DEF_STACK = new ThreadLocal<Stack<String>>();

  private final EnunciateFreemarkerModel model;

  public GenerateExampleJsonMethod(EnunciateFreemarkerModel model) {
    this.model = model;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The generateExampleJson method must have a root element as a parameter.");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    TypeDefinition type;
    int maxDepth = Integer.MAX_VALUE;
    if (object instanceof RootElementDeclaration) {
      RootElementDeclaration rootEl = (RootElementDeclaration) object;
      type = rootEl.getTypeDefinition();
    }
    else if (object instanceof LocalElementDeclaration) {
      LocalElementDeclaration rootEl = (LocalElementDeclaration) object;
      TypeDeclaration elementTypeDeclaration = rootEl.getElementTypeDeclaration();
      if (elementTypeDeclaration instanceof ClassDeclaration) {
        type = model.findTypeDefinition((ClassDeclaration) elementTypeDeclaration);
      }
      else {
        type = null;
      }
    }
    else if (object instanceof TypeDefinition) {
      type = (TypeDefinition) object;
      maxDepth = 2;
    }
    else {
      throw new TemplateModelException("The generateExampleJson method must have a root element as a parameter.");
    }

    try {
      ObjectNode node = generateExampleJson(type, maxDepth);
      StringWriter sw = new StringWriter();
      JsonGenerator generator = new JsonFactory().createJsonGenerator(sw);
      configure(generator);
      node.serialize(generator, null);
      generator.flush();
      sw.flush();
      return sw.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void configure(JsonGenerator generator) {
    generator.useDefaultPrettyPrinter();
    //generator.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
  }

  public ObjectNode generateExampleJson(TypeDefinition type, int maxDepth) {
    if (TYPE_DEF_STACK.get() == null) {
      TYPE_DEF_STACK.set(new Stack<String>());
    }

    ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
    generateExampleJson(type, jsonNode, maxDepth);
    return jsonNode;
  }

  public JsonNode generateExampleJson(EnumTypeDefinition type) {
    Map<String,Object> enumValues = type.getEnumValues();
    Object example = null;
    for (Object value : enumValues.values()) {
      if (value != null) {
        example = value;
        break;
      }
    }

    String exampleValue;
    if (example == null) {
      exampleValue = "...";
    }
    else if (type instanceof QNameEnumTypeDefinition && ((QNameEnumTypeDefinition)type).isUriBaseType()) {
      exampleValue = ((QName) example).getNamespaceURI() + ((QName) example).getLocalPart();
    }
    else {
      exampleValue = String.valueOf(enumValues.values().iterator().next());
    }
    return JsonNodeFactory.instance.textNode(exampleValue);
  }

  protected void generateExampleJson(TypeDefinition type, ObjectNode jsonNode, int maxDepth) {
    if (type != null) {
      if (TYPE_DEF_STACK.get().contains(type.getQualifiedName())) {
        jsonNode.put("...", WhateverNode.instance);
      }
      else {
        TYPE_DEF_STACK.get().push(type.getQualifiedName());
        for (Attribute attribute : type.getAttributes()) {
          generateExampleJson(attribute, jsonNode, maxDepth);
        }
        if (type.getValue() != null) {
          generateExampleJson(type.getValue(), jsonNode, maxDepth);
        }
        else {
          for (Element element : type.getElements()) {
            generateExampleJson(element, jsonNode, maxDepth);
          }
        }
        TYPE_DEF_STACK.get().pop();
      }


      XmlType baseType = type.getBaseType();
      if (baseType instanceof XmlClassType) {
        TypeDefinition typeDef = ((XmlClassType) baseType).getTypeDefinition();
        if (typeDef != null) {
          generateExampleJson(typeDef, jsonNode, maxDepth);
        }
      }
    }
  }

  protected void generateExampleJson(Attribute attribute, ObjectNode jsonNode, int maxDepth) {
    if (TYPE_DEF_STACK.get().size() > maxDepth) {
      return;
    }

    DocumentationExample exampleInfo = attribute.getAnnotation(DocumentationExample.class);
    if (exampleInfo == null || !exampleInfo.exclude()) {
      JsonNode valueNode = generateExampleJson(attribute.getBaseType(), exampleInfo == null || "##default".equals(exampleInfo.value()) ? null : exampleInfo.value(), maxDepth);
      jsonNode.put(attribute.getJsonMemberName(), valueNode);
    }
  }

  protected void generateExampleJson(Value value, ObjectNode jsonNode, int maxDepth) {
    if (TYPE_DEF_STACK.get().size() > maxDepth) {
      return;
    }

    DocumentationExample exampleInfo = value.getAnnotation(DocumentationExample.class);
    if (exampleInfo == null || !exampleInfo.exclude()) {
      JsonNode valueNode = generateExampleJson(value.getBaseType(), exampleInfo == null || "##default".equals(exampleInfo.value()) ? null : exampleInfo.value(), maxDepth);
      jsonNode.put(value.getJsonMemberName(), valueNode);
    }
  }

  protected void generateExampleJson(Element element, ObjectNode jsonNode, int maxDepth) {
    if (TYPE_DEF_STACK.get().size() > maxDepth) {
      return;
    }

    DocumentationExample exampleInfo = element.getAnnotation(DocumentationExample.class);
    if (exampleInfo == null || !exampleInfo.exclude()) {
      String name = element.getJsonMemberName();
      JsonNode elementNode;
      if (!element.isCollectionType()) {
        String exampleValue = exampleInfo == null || "##default".equals(exampleInfo.value()) ? "..." : exampleInfo.value();
        if (!element.isElementRefs() && element.getRef() == null) {
          elementNode = generateExampleJson(element.getBaseType(), exampleValue, maxDepth);
        }
        else {
          elementNode = JsonNodeFactory.instance.objectNode();
        }
      }
      else {
        ArrayNode exampleChoices = JsonNodeFactory.instance.arrayNode();
        for (Element choice : element.getChoices()) {
          QName ref = choice.isElementRefs() ? null : choice.getRef();
          int iterations = "1".equals(choice.getMaxOccurs()) ? 1 : 2;
          for (int i = 0; i < iterations; i++) {
            if (!choice.isElementRefs() && ref == null) {
              String exampleValue = exampleInfo == null || "##default".equals(exampleInfo.value()) ? null : exampleInfo.value();
              XmlType xmlType = choice.getBaseType();
              if (i == 0) {
                exampleChoices.add(generateExampleJson(xmlType, exampleValue, maxDepth));
              }
              else {
                exampleChoices.add(WhateverNode.instance);
              }
            }
            else {
              exampleChoices.add(JsonNodeFactory.instance.objectNode());
            }
          }
        }
        elementNode = exampleChoices;
      }
      jsonNode.put(name, elementNode);
    }
  }

  protected JsonNode generateExampleJson(XmlType type, String specifiedValue, int maxDepth) {
    if (type instanceof XmlClassType) {
      TypeDefinition typeDef = ((XmlClassType) type).getTypeDefinition();
      if (typeDef instanceof EnumTypeDefinition) {
        return generateExampleJson((EnumTypeDefinition) typeDef);
      }
      else {
        return generateExampleJson(typeDef, maxDepth);
      }
    }
    else if (type instanceof MapXmlType) {
      XmlType keyType = ((MapXmlType) type).getKeyType();
      XmlType valueType = ((MapXmlType) type).getValueType();
      ArrayNode jsonNode = JsonNodeFactory.instance.arrayNode();
      for (int i = 0; i < 2; i++) {
        ObjectNode entryNode = JsonNodeFactory.instance.objectNode();
        if (i == 0) {
          entryNode.put("...", generateExampleJson(valueType, null, maxDepth));
          entryNode.put("...", WhateverNode.instance);
        }
        jsonNode.add(entryNode);
      }
      return jsonNode;
    }
    else if (type instanceof XmlPrimitiveType) {
      switch (((XmlPrimitiveType)type).getKind()) {
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
    else if (type instanceof KnownXmlType) {
      switch ((KnownXmlType)type) {
        case BOOLEAN:
          return JsonNodeFactory.instance.booleanNode("true".equalsIgnoreCase(specifiedValue));
        case BYTE:
        case DECIMAL:
        case DOUBLE:
        case FLOAT:
        case INT:
        case INTEGER:
        case LONG:
        case POSITIVE_INTEGER:
        case SHORT:
        case UNSIGNED_BYTE:
        case UNSIGNED_INT:
        case UNSIGNED_LONG:
        case UNSIGNED_SHORT:
          return specifiedValue == null ? WhateverNode.instance : new RawValueNode(specifiedValue);
        case ANY_TYPE:
          return JsonNodeFactory.instance.objectNode();
        case ANY_SIMPLE_TYPE:
          return WhateverNode.instance;
        default:
          return JsonNodeFactory.instance.textNode(specifiedValue == null ? "..." : specifiedValue);
      }
    }
    else {
      return specifiedValue == null ? WhateverNode.instance : new RawValueNode(specifiedValue);
    }
  }

}