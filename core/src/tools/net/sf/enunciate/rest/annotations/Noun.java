package net.sf.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Customizes the noun associated with the annotated method.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.METHOD )
public @interface Noun {

  /**
   * The name of the noun. The default is the name of the method it annotates.
   *
   * @return The name of the noun.
   */
  String value();

}
