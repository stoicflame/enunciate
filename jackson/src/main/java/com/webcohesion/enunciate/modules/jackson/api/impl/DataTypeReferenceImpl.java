package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.modules.jackson.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.SimpleTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonArrayType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonMapType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;

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

  public DataTypeReferenceImpl(JsonType jsonType) {
    String label;
    LinkedList<ContainerType> containers = null;
    String slug = null;
    DataType dataType = null;

    while (jsonType instanceof JsonArrayType || (jsonType instanceof JsonClassType && ((JsonClassType) jsonType).getTypeDefinition() instanceof SimpleTypeDefinition)) {
      if (jsonType instanceof JsonArrayType) {
        containers = containers == null ? new LinkedList<ContainerType>() : containers;
        containers.push(ContainerType.array);
      }
      else {
        jsonType = ((JsonClassType) jsonType).getTypeDefinition().getBaseType();
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
    }


    this.label = label;
    this.slug = slug;
    this.containers = containers;
    this.dataType = dataType;
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
