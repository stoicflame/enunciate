package org.codehaus.enunciate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare a "facet" for the sake of grouping resources and APIs together for simpler browsing.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD ,ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface Facet {

  /**
   * The name of the facet.
   *
   * @return The name of the facet.
   */
  String name();

  /**
   * The value of the facet.
   *
   * @return The value of the facet.
   */
  String value();

  /**
   * Any comments or documentation associated with this facet.
   *
   * @return Any comments or documentation associated with this facet.
   */
  String documentation() default "##default";
}
