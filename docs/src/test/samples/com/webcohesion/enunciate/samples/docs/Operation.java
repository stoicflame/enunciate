package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "operationType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationA.class, name = "A"),
        @JsonSubTypes.Type(value = OperationB.class, name = "B")})
public abstract class Operation {
  public enum OperationType {
    A,
    B
  }

  public OperationType operationType;
}
