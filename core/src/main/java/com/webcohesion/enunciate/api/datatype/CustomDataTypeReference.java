/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.api.datatype;

import java.util.List;

public class CustomDataTypeReference implements DataTypeReference {

  private final BaseType baseType;
  private final String baseTypeFormat;

  public CustomDataTypeReference(BaseType baseType) {
    this(baseType, null);
  }

  public CustomDataTypeReference(BaseType baseType, String baseTypeFormat) {
    this.baseType = baseType;
    if (baseType == null) {
      throw new NullPointerException();
    }
    this.baseTypeFormat = baseTypeFormat;
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
  public String getBaseTypeFormat() {
    return this.baseTypeFormat;
  }

  @Override
  public Example getExample() {
    return null;
  }
}
