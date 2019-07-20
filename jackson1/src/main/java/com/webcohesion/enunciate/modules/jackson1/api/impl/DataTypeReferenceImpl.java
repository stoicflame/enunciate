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
package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.modules.jackson1.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.SimpleTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.types.*;

import javax.lang.model.type.TypeKind;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class DataTypeReferenceImpl implements DataTypeReference {

  private final String label;
  private final String slug;
  private final List<ContainerType> containers;
  private final DataType dataType;
  private final JsonType jsonType;
  private final ApiRegistrationContext registrationContext;

  public DataTypeReferenceImpl(JsonType jsonType, ApiRegistrationContext registrationContext) {
    String label;
    LinkedList<ContainerType> containers = null;
    String slug = null;
    DataType dataType = null;

    while (jsonType instanceof JsonArrayType || jsonType instanceof JsonMapType || (jsonType instanceof JsonClassType && ((JsonClassType) jsonType).getTypeDefinition() instanceof SimpleTypeDefinition)) {
      if (jsonType instanceof JsonArrayType) {
        containers = containers == null ? new LinkedList<ContainerType>() : containers;
        containers.add(ContainerType.array);
        jsonType = ((JsonArrayType) jsonType).getComponentType();
      }
      else if (jsonType instanceof JsonMapType) {
        containers = containers == null ? new LinkedList<ContainerType>() : containers;
        containers.add(ContainerType.map);
        jsonType = ((JsonMapType) jsonType).getValueType();
      }
      else if (((JsonClassType) jsonType).getTypeDefinition() instanceof EnumTypeDefinition) {
        break;
      }
      else {
        jsonType = ((SimpleTypeDefinition) ((JsonClassType) jsonType).getTypeDefinition()).getBaseType();
      }
    }

    if (jsonType instanceof JsonClassType) {
      TypeDefinition typeDef = ((JsonClassType) jsonType).getTypeDefinition();
      if (typeDef instanceof ObjectTypeDefinition) {
        dataType = new ObjectDataTypeImpl((ObjectTypeDefinition) typeDef, registrationContext);
      }
      else if (typeDef instanceof EnumTypeDefinition) {
        dataType = new EnumDataTypeImpl((EnumTypeDefinition) typeDef, registrationContext);
      }
      else {
        throw new IllegalStateException();
      }
      label = dataType.getLabel();
      slug = dataType.getSlug();
    }
    else if (jsonType instanceof JsonMapType) {
      label = "object";
    }
    else {
      label = jsonType.isBoolean() ? "boolean" : jsonType.isNumber() ? "number" : jsonType.isString() ? "string" : "object";

      if (jsonType.isArray()) {
        containers = containers == null ? new LinkedList<ContainerType>() : containers;
        containers.add(ContainerType.array);
      }
    }


    this.jsonType = jsonType;
    this.label = label;
    this.slug = slug;
    this.containers = containers;
    this.dataType = dataType;
    this.registrationContext = registrationContext;
  }

  public JsonType getJsonType() {
    return jsonType;
  }

  @Override
  public BaseType getBaseType() {
    return jsonType.isBoolean() ? BaseType.bool : jsonType.isNumber() ? BaseType.number : jsonType.isString() ? BaseType.string : BaseType.object;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public String getSlug() {
    return this.slug;
  }

  @Override
  public List<ContainerType> getContainers() {
    return this.containers;
  }

  @Override
  public DataType getValue() {
    return this.dataType;
  }

  @Override
  public Example getExample() {
    Example example = null;
    if (this.dataType instanceof ObjectDataTypeImpl) {
      ObjectTypeDefinition typeDefinition = ((ObjectDataTypeImpl) this.dataType).typeDefinition;
      example = typeDefinition == null || typeDefinition.getContext().isDisableExamples() ? null : new DataTypeExampleImpl(typeDefinition, this.containers, registrationContext);
    }
    else if (this.dataType instanceof EnumDataTypeImpl) {
      String body = "...";
      List<? extends Value> values = this.dataType.getValues();
      if (values != null && values.isEmpty()) {
        body = values.get(0).getValue();
      }
      example = new CustomExampleImpl(body);
    }
    return example;
  }

  @Override
  public BaseTypeFormat getBaseTypeFormat() {
    if (this.jsonType instanceof JsonPrimitiveType) {
      TypeKind kind = ((JsonPrimitiveType) this.jsonType).getKind();
      switch (kind) {
        case INT:
          return BaseTypeFormat.INT32;
        case LONG:
          return BaseTypeFormat.INT64;
        case FLOAT:
          return BaseTypeFormat.FLOAT;
        case DOUBLE:
          return BaseTypeFormat.DOUBLE;
      }
    }

    return null;
  }
}
