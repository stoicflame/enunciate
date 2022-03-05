package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Ryan Heaton
 */
public abstract class CompoundIdMixin {

  @JsonCreator
  public CompoundIdMixin(String value) {
  }

  @JsonValue
  public abstract String toValue();
}
