package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ExplicitDataTypeReference implements DataTypeReference {

  private final DataType dataType;

  public ExplicitDataTypeReference(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public String getLabel() {
    return this.dataType.getLabel();
  }

  @Override
  public String getSlug() {
    return this.dataType.getSlug();
  }

  @Override
  public BaseType getBaseType() {
    return this.dataType.getBaseType();
  }

  @Override
  public List<ContainerType> getContainers() {
    return null;
  }

  @Override
  public DataType getValue() {
    return this.dataType;
  }
}
