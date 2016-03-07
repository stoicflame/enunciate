package com.webcohesion.enunciate.metadata;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to mark a method as an example method to be used in the generated documentation.
 *
 * @author Ryan Heaton
 */
@Target ( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface DocumentationExample {

  /**
   * Whether to exclude this example.
   *
   * @return Whether to exclude this example.
   */
  boolean exclude() default false;

  /**
   * The value of this documentation example.
   *
   * @return The value of this documentation example.
   */
  String value() default "##default";

  /**
   * A second value for this documentation example to be used e.g. for arrays.
   *
   * @return A second value for this documentation example to be used e.g. for arrays.
   */
  String value2() default "##default";

}
