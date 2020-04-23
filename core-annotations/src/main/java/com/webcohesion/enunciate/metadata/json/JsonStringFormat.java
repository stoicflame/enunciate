package com.webcohesion.enunciate.metadata.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a JSON format for a string. See https://json-schema.org/understanding-json-schema/reference/string.html#format
 */
@Target(
   { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD }
)
@Retention(
   RetentionPolicy.RUNTIME
)
public @interface JsonStringFormat {

  String value();
}
