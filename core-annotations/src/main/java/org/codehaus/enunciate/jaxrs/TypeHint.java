package org.codehaus.enunciate.jaxrs;

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
  Class<?> value();
}
