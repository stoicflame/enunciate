package com.webcohesion.enunciate.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare custom label for a resource or data type.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.TYPE }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface Label {

  /**
   * The value of the custom label.
   *
   * @return The value of the custom label.
   */
  String value();

}
