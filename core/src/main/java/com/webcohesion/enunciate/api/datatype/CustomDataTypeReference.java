/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.api.datatype;

import java.util.List;

public class CustomDataTypeReference implements DataTypeReference {

  /**
   * The format that OpenAPI uses to describe a payload of opaque bytes.
   */
  private static final String BINARY_FORMAT = "binary";

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

  /**
   * A reference to an opaque, binary payload.
   *
   * @return A reference to an opaque, binary payload.
   */
  public static CustomDataTypeReference binary() {
    return new CustomDataTypeReference(BaseType.string, BINARY_FORMAT);
  }

  @Override
  public String getLabel() {
    if (BINARY_FORMAT.equals(this.baseTypeFormat)) {
      //a payload of opaque bytes; "string" would be misleading.
      return BINARY_FORMAT;
    }

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
