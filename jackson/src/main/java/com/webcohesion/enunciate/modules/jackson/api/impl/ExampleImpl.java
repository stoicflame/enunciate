package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;

/**
 * @author Ryan Heaton
 */
public class ExampleImpl implements Example {

  private final TypeDefinition type;

  public ExampleImpl(TypeDefinition type) {
    this.type = type;
  }

  @Override
  public String getLang() {
    return "js";
  }

  @Override
  public String getBody() {
    //todo:
    return null;
  }
}
