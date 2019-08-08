package com.webcohesion.enunciate.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field or parameter holds a password.
 */
@Target(
   { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER }
)
@Retention(
   RetentionPolicy.RUNTIME
)
public @interface Password {
}
