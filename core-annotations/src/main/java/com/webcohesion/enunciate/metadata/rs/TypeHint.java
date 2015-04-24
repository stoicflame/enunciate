package com.webcohesion.enunciate.metadata.rs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to give Enunciate a hint about what a JAX-RS resource method returns or accepts as an input parameter.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.PARAMETER, ElementType.METHOD }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface TypeHint {

  /**
   * The hint.
   *
   * @return The hint.
   */
  Class<?> value() default NONE.class;

  /**
   * The fully-qualified classname of the hint. (Used in the case that the hint isn't on the classpath of the resource method at build-time.)
   *
   * @return The fully-qualified classname of the hint.
   */
  String qualifiedName() default "##NONE";

  /**
   * Class indicating "no content" for a resource type.
   */
  public static final class NO_CONTENT {}

  public static final class NONE {}
}
