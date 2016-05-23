package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Value;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ValueImpl implements Value {

  private final String value;
  private final String description;
  private final Set<String> styles;

  public ValueImpl(String value, String description, Set<String> styles) {
    this.value = value;
    this.description = description;
    this.styles = styles;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Set<String> getStyles() {
    return this.styles;
  }
}
