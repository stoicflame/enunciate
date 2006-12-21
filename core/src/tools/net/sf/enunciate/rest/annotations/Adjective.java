package net.sf.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that the annotated parameter is an adjective that describes the noun
 * of its associated method.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.PARAMETER )
public @interface Adjective {

  /**
   * The name of the adjective.
   *
   * @return The name of the adjective.
   */
  String name();

}
