package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.jackson.model.*;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonArrayType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonMapType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;

import java.util.LinkedList;

/**
 * @author Ryan Heaton
 */
public class ExampleImpl implements Example {

  private final ObjectTypeDefinition type;

  public ExampleImpl(ObjectTypeDefinition type) {
    this.type = type;
  }

  @Override
  public String getLang() {
    return "js";
  }

  @Override
  public String getBody() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    build(node, this.type, new LinkedList<String>());

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    try {
      return mapper.writeValueAsString(node);
    }
    catch (JsonProcessingException e) {
      throw new EnunciateException(e);
    }
  }

  private void build(ObjectNode node, ObjectTypeDefinition type, LinkedList<String> contextStack) {
    if (contextStack.size() > 2) {
      //don't go deeper than 2 for fear of the OOM (see https://github.com/stoicflame/enunciate/issues/139).
      return;
    }

    for (Member member : type.getMembers()) {
      if (ElementUtils.findDeprecationMessage(member) != null) {
        continue;
      }

      String example = null;
      DocumentationExample documentationExample = member.getAnnotation(DocumentationExample.class);
      if (documentationExample != null) {
        if (documentationExample.exclude()) {
          continue;
        }
        else if (!"##default".equals(documentationExample.value())) {
          example = documentationExample.value();
        }
      }

      if (member.getChoices().size() > 1) {
        if (member.isCollectionType()) {
          final ArrayNode exampleNode = JsonNodeFactory.instance.arrayNode();

          for (Member choice : member.getChoices()) {
            JsonType jsonType = choice.getJsonType();
            String choiceName = choice.getName();
            if ("".equals(choiceName)) {
              choiceName = "...";
            }

            if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_ARRAY) {
              ArrayNode wrapperNode = JsonNodeFactory.instance.arrayNode();
              wrapperNode.add(choiceName);
              wrapperNode.add(exampleNode(jsonType, example, contextStack));
              exampleNode.add(wrapperNode);
            }
            else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_OBJECT) {
              ObjectNode wrapperNode = JsonNodeFactory.instance.objectNode();
              wrapperNode.set(choiceName, exampleNode(jsonType, example, contextStack));
              exampleNode.add(wrapperNode);
            }
            else {
              exampleNode.add(exampleNode(jsonType, example, contextStack));
            }
          }

          node.set(member.getName(), exampleNode);
        }
        else {
          for (Member choice : member.getChoices()) {
            JsonNode exampleNode;
            JsonType jsonType = choice.getJsonType();
            String choiceName = choice.getName();
            if ("".equals(choiceName)) {
              choiceName = "...";
            }

            if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_ARRAY) {
              ArrayNode wrapperNode = JsonNodeFactory.instance.arrayNode();
              wrapperNode.add(choiceName);
              wrapperNode.add(exampleNode(jsonType, example, contextStack));
              exampleNode = wrapperNode;
            }
            else if (member.getSubtypeIdInclusion() == JsonTypeInfo.As.WRAPPER_OBJECT) {
              ObjectNode wrapperNode = JsonNodeFactory.instance.objectNode();
              wrapperNode.set(choiceName, exampleNode(jsonType, example, contextStack));
              exampleNode = wrapperNode;
            }
            else {
              exampleNode = exampleNode(jsonType, example, contextStack);
            }

            node.set(member.getName(), exampleNode);
          }
        }
      }
      else {
        node.set(member.getName(), exampleNode(member.getJsonType(), example, contextStack));
      }
    }

    JsonType supertype = type.getSupertype();
    if (supertype instanceof JsonClassType && ((JsonClassType)supertype).getTypeDefinition() instanceof ObjectTypeDefinition) {
      build(node, (ObjectTypeDefinition) ((JsonClassType) supertype).getTypeDefinition(), contextStack);
    }

    if (type.getWildcardMember() != null && ElementUtils.findDeprecationMessage(type.getWildcardMember()) == null) {
      node.put("extension1", "...");
      node.put("extension2", "...");
    }

  }

  private JsonNode exampleNode(JsonType jsonType, String specifiedExample, LinkedList<String> contextStack) {
    if (jsonType instanceof JsonClassType) {
      TypeDefinition typeDefinition = ((JsonClassType) jsonType).getTypeDefinition();
      if (typeDefinition instanceof ObjectTypeDefinition) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        if (!contextStack.contains(typeDefinition.getQualifiedName().toString())) {
          contextStack.push(typeDefinition.getQualifiedName().toString());
          try {
            build(objectNode, (ObjectTypeDefinition) typeDefinition, contextStack);
          }
          finally {
            contextStack.pop();
          }
        }
        return objectNode;
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        String example = "???";

        if (specifiedExample != null) {
          example = specifiedExample;
        }
        else if (((EnumTypeDefinition) typeDefinition).getEnumValues().size() > 0) {
          example = ((EnumTypeDefinition) typeDefinition).getEnumValues().values().iterator().next();
        }

        return JsonNodeFactory.instance.textNode(example);
      }
      else {
        return exampleNode(((SimpleTypeDefinition) typeDefinition).getBaseType(), specifiedExample, contextStack);
      }
    }
    else if (jsonType instanceof JsonMapType) {
      ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
      mapNode.put("property1", "...");
      mapNode.put("property2", "...");
      return mapNode;
    }
    else if (jsonType.isArray()) {
      ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
      JsonNode componentNode = exampleNode(((JsonArrayType) jsonType).getComponentType(), specifiedExample, contextStack);
      arrayNode.add(componentNode);
      arrayNode.add(componentNode);
      return arrayNode;
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
}
