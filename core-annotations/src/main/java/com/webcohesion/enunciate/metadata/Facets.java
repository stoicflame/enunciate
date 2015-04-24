package com.webcohesion.enunciate.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to apply a set of facets to an API component.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD ,ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface Facets {

  /**
   * The applicable facets.
   *
   * @return The applicable facets.
   */
  Facet[] value();
}
