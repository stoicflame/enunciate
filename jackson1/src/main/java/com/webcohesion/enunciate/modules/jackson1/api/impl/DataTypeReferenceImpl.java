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

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.modules.jackson1.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.SimpleTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonArrayType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonMapType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;

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

  public DataTypeReferenceImpl(JsonType jsonType) {
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
        dataType = new ObjectDataTypeImpl((ObjectTypeDefinition) typeDef);
      }
      else if (typeDef instanceof EnumTypeDefinition) {
        dataType = new EnumDataTypeImpl((EnumTypeDefinition) typeDef);
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
}
