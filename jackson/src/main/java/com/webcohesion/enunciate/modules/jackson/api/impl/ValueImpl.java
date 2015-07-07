package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Value;

/**
 * @author Ryan Heaton
 */
public class ValueImpl implements Value {

  private final String value;
  private final String description;

  public ValueImpl(String value, String description) {
    this.value = value;
    this.description = description;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
