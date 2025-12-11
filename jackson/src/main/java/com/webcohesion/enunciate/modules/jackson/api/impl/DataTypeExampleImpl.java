/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.jackson.model.*;
import com.webcohesion.enunciate.modules.jackson.model.types.*;
import com.webcohesion.enunciate.util.ExampleUtils;
import com.webcohesion.enunciate.util.TypeHintUtils;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author Ryan Heaton
 */
public class DataTypeExampleImpl extends ExampleImpl {

  private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private final ObjectTypeDefinition type;
  private final List<DataTypeReference.ContainerType> containers;
  private final ApiRegistrationContext registrationContext;

  public DataTypeExampleImpl(ObjectTypeDefinition type, ApiRegistrationContext registrationContext) {
    this(type, null, registrationContext);
  }

  public DataTypeExampleImpl(ObjectTypeDefinition typeDefinition, List<DataTypeReference.ContainerType> containers, ApiRegistrationContext registrationContext) {
    this.type = typeDefinition;
    this.containers = containers == null ? Collections.<DataTypeReference.ContainerType>emptyList() : containers;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getBody() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    Context context = new Context();
    context.stack = new LinkedList<String>();
    build(node, this.type, this.type, context);

    if (this.type.getContext().isWrapRootValue()) {
      ObjectNode wrappedNode = JsonNodeFactory.instance.objectNode();
      wrappedNode.set(this.type.getJsonRootName(), node);
      node = wrappedNode;
    }

    if (isWrappedSubclass(this.type)) {
      ObjectNode wrappedNode = JsonNodeFactory.instance.objectNode();
      wrappedNode.set(this.type.getTypeIdValue(), node);
      node = wrappedNode;
    }

    JsonNode outer = node;
    for (DataTypeReference.ContainerType container : this.containers) {
      switch (container) {
        case array:
        case collection:
        case list:
          ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
          arrayNode.add(outer);
          outer = arrayNode;
          break;
        case map:
          ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
          mapNode.set("...", outer);
          outer = mapNode;
          break;
      }
    }

    try {
      return MAPPER.writeValueAsString(outer);
    }
    catch (JsonProcessingException e) {
      throw new EnunciateException(e);
    }
  }

  private boolean isWrappedSubclass(ObjectTypeDefinition type) {
    if (type.isAbstract() || type.isInterface()) {
      return false;
    }

    JsonType supertype = type.getSupertype();
    if (supertype instanceof JsonClassType) {
      TypeDefinition typeDefinition = ((JsonClassType) supertype).getTypeDefinition();
      if (typeDefinition.getTypeIdInclusion() == JsonTypeInfo.As.WRAPPER_OBJECT) {
        return true;
      }
      else if (typeDefinition instanceof ObjectTypeDefinition) {
        return isWrappedSubclass((ObjectTypeDefinition) typeDefinition);
      }
    }
    return false;
  }

  private void build(ObjectNode node, ObjectTypeDefinition type, @Nonnull ObjectTypeDefinition sourceType, Context context) {
    if (context.stack.size() > 2) {
      //don't go deeper than 2 for fear of the OOM (see https://github.com/stoicflame/enunciate/issues/139).
      return;
    }

    if (type.getTypeIdInclusion() == JsonTypeInfo.As.PROPERTY) {
      if (type.getTypeIdProperty() != null) {
        node.put(type.getTypeIdProperty(), sourceType.getTypeIdValue());
      }
    }

    JsonNode override = findExampleOverride(type, type.getContext().getContext());
    if (override != null) {
      if (override instanceof ObjectNode) {
        node.setAll((ObjectNode) override);
        return;
      }
      else {
        type.getContext().getContext().getLogger().warn("JSON example override of %s can't be used because it's not a JSON object.", type.getQualifiedName());
      }
    }

    FacetFilter facetFilter = this.registrationContext.getFacetFilter();
    for (Member member : type.getMembers()) {
      if (node.has(member.getName())) {
        continue;
      }

      if (!facetFilter.accept(member)) {
        continue;
      }

      if (ElementUtils.findDeprecationMessage(member, null) != null) {
        continue;
      }

      JsonNode memberOverride = findExampleOverride(member, type.getContext().getContext());
      if (memberOverride != null) {
        node.set(member.getName(), memberOverride);
        continue;
      }

      String example = null;
      String example2 = null;
      JsonType exampleType = null;

      JavaDoc.JavaDocTagList tags = getDocumentationExampleTags(member);
      List<String> exampleValues = ExampleUtils.readValues(tags, type.getContext().getContext());
      if (exampleValues != null && !exampleValues.isEmpty()) {
        example = exampleValues.get(0);
        if (exampleValues.size() > 1) {
          example2 = exampleValues.get(1);
        }
      }

      tags = member.getJavaDoc().get("documentationType");
      if (tags != null && tags.size() > 0) {
        String tag = tags.get(0).trim();
        if (!tag.isEmpty()) {
          TypeElement typeElement = type.getContext().getContext().getProcessingEnvironment().getElementUtils().getTypeElement(tag);
          if (typeElement != null) {
            exampleType = JsonTypeFactory.getJsonType(typeElement.asType(), type.getContext());
          }
          else {
            type.getContext().getContext().getLogger().warn("Invalid documentation type %s.", tag);
          }
        }
      }

      DocumentationExample documentationExample = getDocumentationExample(member);
      if (documentationExample != null) {
        if (documentationExample.exclude()) {
          continue;
        }

        example = documentationExample.value();
        example = "##default".equals(example) ? null : example;
        example2 = documentationExample.value2();
        example2 = "##default".equals(example2) ? null : example2;
        TypeMirror typeHint = TypeHintUtils.getTypeHint(documentationExample.type(), type.getContext().getContext().getProcessingEnvironment(), null);
        if (typeHint != null) {
          exampleType = JsonTypeFactory.getJsonType(typeHint, type.getContext());
        }
      }

      String specifiedTypeInfoValue = findSpecifiedTypeInfoValue(member, type.getQualifiedName().toString(), type);
      if (specifiedTypeInfoValue != null) {
        example = specifiedTypeInfoValue;
        example2 = specifiedTypeInfoValue;
      }

      String configuredExample = getConfiguredExample(member);
      if (configuredExample != null) {
        example = configuredExample;
        example2 = configuredExample;
      }

      if (context.currentIndex % 2 > 0) {
        //if our index is odd, switch example 1 and example 2.
        String placeholder = example2;
        example2 = example;
        example = placeholder;
      }

      if (member.getChoices().size() > 1) {
        if (member.isCollectionType()) {
          final ArrayNode exampleNode = JsonNodeFactory.instance.arrayNode();

          for (Member choice : member.getChoices()) {
            JsonType jsonType = exampleType == null ? choice.getJsonType() : exampleType;
            String choiceName = choice.getName();
            if ("".equals(choiceName)) {
              choiceName = "...";
            }

            if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_ARRAY) {
              ArrayNode wrapperNode = JsonNodeFactory.instance.arrayNode();
              wrapperNode.add(choiceName);
              wrapperNode.add(exampleNode(jsonType, example, example2, context));
              exampleNode.add(wrapperNode);
            }
            else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_OBJECT) {
              ObjectNode wrapperNode = JsonNodeFactory.instance.objectNode();
              wrapperNode.set(choiceName, exampleNode(jsonType, example, example2, context));
              exampleNode.add(wrapperNode);
            }
            else {
              JsonNode itemNode = exampleNode(jsonType, example, example2, context);

              if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.PROPERTY) {
                if (member.getSubtypeIdProperty() != null && itemNode instanceof ObjectNode) {
                  ((ObjectNode) itemNode).put(member.getSubtypeIdProperty(), "...");
                }
              }
              else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                if (member.getSubtypeIdProperty() != null) {
                  node.put(member.getSubtypeIdProperty(), "...");
                }
              }

              exampleNode.add(itemNode);
            }
          }

          node.set(member.getName(), exampleNode);
        }
        else {
          for (Member choice : member.getChoices()) {
            JsonNode exampleNode;
            JsonType jsonType = exampleType == null ? choice.getJsonType() : exampleType;
            String choiceName = choice.getName();
            if ("".equals(choiceName)) {
              choiceName = "...";
            }

            if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_ARRAY) {
              ArrayNode wrapperNode = JsonNodeFactory.instance.arrayNode();
              wrapperNode.add(choiceName);
              wrapperNode.add(exampleNode(jsonType, example, example2, context));
              exampleNode = wrapperNode;
            }
            else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_OBJECT) {
              ObjectNode wrapperNode = JsonNodeFactory.instance.objectNode();
              wrapperNode.set(choiceName, exampleNode(jsonType, example, example2, context));
              exampleNode = wrapperNode;
            }
            else {
              exampleNode = exampleNode(jsonType, example, example2, context);

              if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.PROPERTY) {
                if (member.getSubtypeIdProperty() != null && exampleNode instanceof ObjectNode) {
                  ((ObjectNode) exampleNode).put(member.getSubtypeIdProperty(), "...");
                }
              }
              else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
                if (member.getSubtypeIdProperty() != null) {
                  node.put(member.getSubtypeIdProperty(), "...");
                }
              }
            }

            node.set(member.getName(), exampleNode);
          }
        }
      }
      else {
        JsonType jsonType = exampleType == null ? member.getJsonType() : exampleType;
        node.set(member.getName(), exampleNode(jsonType, example, example2, context));
      }
    }

    JsonType supertype = type.getSupertype();
    if (supertype instanceof JsonClassType && ((JsonClassType) supertype).getTypeDefinition() instanceof ObjectTypeDefinition) {
      build(node, (ObjectTypeDefinition) ((JsonClassType) supertype).getTypeDefinition(), sourceType, context);
    }

    if (type.getWildcardMember() != null && ElementUtils.findDeprecationMessage(type.getWildcardMember(), null) == null
       && !ExampleUtils.isExcluded(type.getWildcardMember())) {
      node.put("extension1", "...");
      node.put("extension2", "...");
    }

  }

  private JsonNode findExampleOverride(DecoratedElement el, EnunciateContext context) {
    String overrideValue = null;

    JavaDoc.JavaDocTagList overrideTags = el.getJavaDoc().get("jsonExampleOverride");
    List<String> exampleValues = ExampleUtils.readValues(overrideTags, context);
    if (exampleValues != null && !exampleValues.isEmpty()) {
      overrideValue = exampleValues.get(0);
    }

    DocumentationExample annotation = (DocumentationExample) el.getAnnotation(DocumentationExample.class);
    if (annotation != null && !"##default".equals(annotation.jsonOverride())) {
      overrideValue = annotation.jsonOverride();
    }

    if (overrideValue != null) {
      try {
        return MAPPER.readTree(overrideValue);
      }
      catch (Exception e) {
        context.getLogger().error("Unable to parse example override of element %s: %s", el.toString(), e.getMessage());
      }
    }

    return null;
  }

  private DocumentationExample getDocumentationExample(Member member) {
    DocumentationExample annotation = member.getAnnotation(DocumentationExample.class);
    if (annotation == null) {
      DecoratedTypeMirror accessorType = member.getBareAccessorType();
      if (accessorType instanceof DecoratedDeclaredType) {
        annotation = ((DecoratedDeclaredType) accessorType).asElement().getAnnotation(DocumentationExample.class);
      }
    }
    return annotation;
  }

  private JavaDoc.JavaDocTagList getDocumentationExampleTags(Member member) {
    JavaDoc.JavaDocTagList tags = member.getJavaDoc().get("documentationExample");
    if (tags == null || tags.isEmpty()) {
      DecoratedTypeMirror accessorType = member.getBareAccessorType();
      if (accessorType instanceof DecoratedDeclaredType) {
        Element element = ((DecoratedDeclaredType) accessorType).asElement();
        tags = element instanceof DecoratedElement ? ((DecoratedElement) element).getJavaDoc().get("documentationExample") : null;
      }
    }
    return tags;
  }

  private String getConfiguredExample(Member member) {
    String configuredExample = null;
    DecoratedTypeMirror accessorType = member.getBareAccessorType();
    if (accessorType instanceof DecoratedDeclaredType) {
      Element element = ((DecoratedDeclaredType) accessorType).asElement();
      if (element instanceof TypeElement) {
        configuredExample = member.getContext().lookupExternalExample((TypeElement) element);
      }
    }
    return configuredExample;
  }

  private String findSpecifiedTypeInfoValue(Member member, String specifiedType, TypeDefinition type) {
    if (type == null) {
      return null;
    }
    else if (type.getTypeIdType() == JsonTypeInfo.Id.NAME && member.getSimpleName().toString().equals(type.getTypeIdProperty())) {
      JsonSubTypes subTypes = type.getAnnotation(JsonSubTypes.class);
      if (subTypes != null) {
        for (final JsonSubTypes.Type element : subTypes.value()) {
          DecoratedTypeMirror choiceType = Annotations.mirrorOf(element::value, type.getContext().getContext().getProcessingEnvironment());

          if (choiceType.isInstanceOf(specifiedType)) {
            return element.name();
          }
        }

        return null;
      }
    }

    JsonType supertype = type instanceof ObjectTypeDefinition ? ((ObjectTypeDefinition) type).getSupertype() : null;
    if (supertype instanceof JsonClassType) {
      return findSpecifiedTypeInfoValue(member, specifiedType, ((JsonClassType) supertype).getTypeDefinition());
    }

    return null;
  }

  private JsonNode exampleNode(JsonType jsonType, String specifiedExample, String specifiedExample2, Context context) {
    if (jsonType instanceof JsonClassType) {
      TypeDefinition typeDefinition = ((JsonClassType) jsonType).getTypeDefinition();
      if (typeDefinition instanceof ObjectTypeDefinition) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        if (!context.stack.contains(typeDefinition.getQualifiedName().toString())) {
          context.stack.push(typeDefinition.getQualifiedName().toString());
          try {
            final ObjectTypeDefinition objTypeDef = (ObjectTypeDefinition) typeDefinition;
            build(objectNode, objTypeDef, objTypeDef, context);
          }
          finally {
            context.stack.pop();
          }
        }
        return objectNode;
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        String example = "???";

        if (specifiedExample != null) {
          example = specifiedExample;
        }
        else {
          List<EnumValue> enumValues = ((EnumTypeDefinition) typeDefinition).getEnumValues();
          if (enumValues.size() > 0) {
            int index = new Random().nextInt(enumValues.size());
            example = enumValues.get(index).getValue();
          }
        }

        JsonType baseType = ((EnumTypeDefinition) typeDefinition).getBaseType();
        if (baseType.isBoolean()) {
          return JsonNodeFactory.instance.booleanNode(Boolean.valueOf(example));
        }
        else if (baseType.isWholeNumber()) {
          Long value;
          try {
            value = Long.valueOf(example);
          }
          catch (NumberFormatException e) {
            value = 123456L;
          }
          return JsonNodeFactory.instance.numberNode(value);
        }
        else if (baseType.isNumber()) {
          Double value;
          try {
            value = Double.valueOf(example);
          }
          catch (NumberFormatException e) {
            value = 12345.67890D;
          }
          return JsonNodeFactory.instance.numberNode(value);
        }
        else {
          return JsonNodeFactory.instance.textNode(example);
        }
      }
      else {
        return exampleNode(((SimpleTypeDefinition) typeDefinition).getBaseType(), specifiedExample, specifiedExample2, context);
      }
    }
    else if (jsonType instanceof JsonMapType) {
      ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
      JsonType valueType = ((JsonMapType) jsonType).getValueType();
      String key1Example = "property1";
      if (specifiedExample != null) {
        int firstSpace = JavaDoc.indexOfFirstWhitespace(specifiedExample);
        if (firstSpace >= 0) {
          key1Example = specifiedExample.substring(0, firstSpace);
          specifiedExample = specifiedExample.substring(firstSpace + 1).trim();
          if (specifiedExample.isEmpty()) {
            specifiedExample = null;
          }
        }
      }

      String key2Example = "property2";
      if (specifiedExample2 != null) {
        int firstSpace = JavaDoc.indexOfFirstWhitespace(specifiedExample2);
        if (firstSpace >= 0) {
          key2Example = specifiedExample2.substring(0, firstSpace);
          specifiedExample2 = specifiedExample2.substring(firstSpace + 1).trim();
          if (specifiedExample2.isEmpty()) {
            specifiedExample2 = null;
          }
        }
      }

      mapNode.set(key1Example, exampleNode(valueType, specifiedExample, specifiedExample2, context));
      Context context2 = new Context();
      context2.stack = context.stack;
      context2.currentIndex = 1;
      mapNode.set(key2Example, exampleNode(valueType, specifiedExample2, specifiedExample, context2));
      return mapNode;
    }
    else if (jsonType.isArray()) {
      ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
      if (jsonType instanceof JsonArrayType) {
        JsonNode componentNode = exampleNode(((JsonArrayType) jsonType).getComponentType(), specifiedExample, specifiedExample2, context);
        arrayNode.add(componentNode);
        Context context2 = new Context();
        context2.stack = context.stack;
        context2.currentIndex = 1;
        JsonNode componentNode2 = exampleNode(((JsonArrayType) jsonType).getComponentType(), specifiedExample2, specifiedExample, context2);
        arrayNode.add(componentNode2);
      }
      return arrayNode;
    }
    else if (jsonType.isWholeNumber()) {
      Long example = 12345L;
      if (specifiedExample != null) {
        try {
          example = Long.parseLong(specifiedExample);
        }
        catch (NumberFormatException e) {
          this.type.getContext().getContext().getLogger().warn("\"%s\" was provided as a documentation example, but it is not a valid JSON whole number, so it will be ignored.", specifiedExample);
        }
      }
      return JsonNodeFactory.instance.numberNode(example);
    }
    else if (jsonType.isNumber()) {
      Double example = 12345D;
      if (specifiedExample != null) {
        try {
          example = Double.parseDouble(specifiedExample);
        }
        catch (NumberFormatException e) {
          this.type.getContext().getContext().getLogger().warn("\"%s\" was provided as a documentation example, but it is not a valid JSON number, so it will be ignored.", specifiedExample);
        }
      }
      return JsonNodeFactory.instance.numberNode(example);
    }
    else if (jsonType.isBoolean()) {
      boolean example = !"false".equals(specifiedExample);
      return JsonNodeFactory.instance.booleanNode(example);
    }
    else if (jsonType.isString()) {
      String example = specifiedExample;
      if (example == null) {
        example = "...";
      }
      return JsonNodeFactory.instance.textNode(example);
    }
    else {
      return JsonNodeFactory.instance.objectNode();
    }
  }

  private static class Context {
    LinkedList<String> stack;
    int currentIndex = 0;
  }
}
