package com.webcohesion.enunciate.metadata.rs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generic holder for a response code. Response codes are used as both the HTTP status response
 * and as HTTP Warnings.
 *
 * @author Ryan Heaton
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( {} )
public @interface ResponseCode {

  /**
   * The code.
   *
   * @return The code.
   */
  int code();

  /**
   * The condition under which the code is supplied.
   *
   * @return The condition under which the code is supplied.
   */
  String condition();

  ResponseHeader[] additionalHeaders() default {};

}
