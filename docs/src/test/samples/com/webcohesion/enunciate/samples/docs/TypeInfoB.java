package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeInfoB extends TypeInfo_Base {
  @JsonProperty("prop_b")
  public String prop;
}
