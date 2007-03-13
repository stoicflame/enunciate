package org.codehaus.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation used on an exception to indicate it's a REST error.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( {ElementType.TYPE} )
public @interface RESTError {

  /**
   * The error code (default 500).
   *
   * @return The error code.
   */
  int errorCode() default 500;

}
