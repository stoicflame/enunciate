package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TypeInfoA.class, name = "A"),
        @JsonSubTypes.Type(value = TypeInfoB.class, name = "B")
})
public abstract class TypeInfo_Base {
}
