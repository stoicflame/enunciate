package org.codehaus.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Customizes the verb associated with the annotated method.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.METHOD )
public @interface Verb {

  /**
   * The verb type.
   *
   * @return The verb type.
   */
  VerbType value();
  
}
