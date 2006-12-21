package net.sf.enunciate.rest.annotations;

/**
 * Annotation used on an exception to indicate it's a REST error.
 *
 * @author Ryan Heaton
 */
public @interface RESTError {

  /**
   * The error code (default 500).
   *
   * @return The error code.
   */
  int errorCode() default 500;
  
}
