package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

@JsonInclude
public class TypeWithHintOnProperty {
  @TypeHint(PropertyTypeHint.class)
  public PropertyTypeActual someProperty;
}
