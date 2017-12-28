package com.webcohesion.enunciate.samples.docs;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Just an enum
 */
@JsonInclude
public enum CountEnum {
    /**
     * <p>Here is an enum for a value that comes first.</p>
     */
    FIRST,

    /**
     * <p>Here is an enum for a value that comes second.</p>
     */
    SECOND,

    /**
     * <p>Here is an enum for a value that comes third.</p>
     */
    THIRD;

    public final OtherEnum otherEnum = OtherEnum.VALUE;
}
