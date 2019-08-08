/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.api.datatype;

import java.util.List;

public class CustomDataTypeReference implements DataTypeReference {

  private final BaseType baseType;

  public CustomDataTypeReference(BaseType baseType) {
    this.baseType = baseType;
    if (baseType == null) {
      throw new NullPointerException();
    }
  }

  @Override
  public String getLabel() {
    return this.baseType == BaseType.bool ? "boolean" : this.baseType.name();
  }

  @Override
  public String getSlug() {
    return null;
  }

  @Override
  public List<ContainerType> getContainers() {
    return null;
  }

  @Override
  public DataType getValue() {
    return null;
  }

  @Override
  public BaseType getBaseType() {
    return this.baseType;
  }

  @Override
  public BaseTypeFormat getBaseTypeFormat() {
    return null;
  }

  @Override
  public Example getExample() {
    return null;
  }
}
