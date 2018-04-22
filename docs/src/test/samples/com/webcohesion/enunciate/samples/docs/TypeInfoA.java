package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeInfoA extends TypeInfo_Base {
  @JsonProperty("prop_a")
  public String prop;
}
