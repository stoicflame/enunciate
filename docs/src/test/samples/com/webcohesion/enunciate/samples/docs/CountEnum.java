package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Just an enum
 */
@JsonInclude
public enum CountEnum {
    /**
     * <p>Here is an enum for a value that comes first.</p>
     */
    @JsonProperty("one")
    FIRST,

    /**
     * <p>Here is an enum for a value that comes second.</p>
     */
    @JsonProperty("two")
    SECOND,

    /**
     * <p>Here is an enum for a value that comes third.</p>
     */
    @JsonProperty("three")
    THIRD;

    public final OtherEnum otherEnum = OtherEnum.VALUE;
}
