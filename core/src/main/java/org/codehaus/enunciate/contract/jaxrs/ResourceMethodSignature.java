package org.codehaus.enunciate.contract.jaxrs;

import javax.ws.rs.*;

/**
 * Annotation used to "override" the method signature of a REST method that doesn't conform to JAX-RS specification. Used for
 * purposes of leveraging the documentation features of Enunciate without requiring your methods to conform to JAX-RS.
 *
 * @author Ryan Heaton
 */
public @interface ResourceMethodSignature {

  /**
   * The input type for this resource method (i.e. the "entity parameter").
   *
   * @return The class the defines the "entity parameter"
   */
  Class<?> input() default NONE.class;

  /**
   * The output type for this resource method (what would normally be the JAX-RS return type).
   *
   * @return The output type for this resource method.
   */
  Class<?> output() default NONE.class;

  /**
   * The set of matrix parameters applicable to this resource method.
   * 
   * @return The set of matrix parameters applicable to this resource method.
   */
  MatrixParam[] matrixParams() default {};

  /**
   * The set of query parameters applicable to this resource method.
   * 
   * @return The set of query parameters applicable to this resource method.
   */
  QueryParam[] queryParams() default {};

  /**
   * The set of path parameters applicable to this resource method.
   * 
   * @return The set of path parameters applicable to this resource method.
   */
  PathParam[] pathParams() default {};

  /**
   * The set of cookie parameters applicable to this resource method.
   * 
   * @return The set of cookie parameters applicable to this resource method.
   */
  CookieParam[] cookieParams() default {};

  /**
   * The set of header parameters applicable to this resource method.
   * 
   * @return The set of header parameters applicable to this resource method.
   */
  HeaderParam[] headerParams() default {};

  /**
   * The set of form parameters applicable to this resource method.
   * 
   * @return The set of form parameters applicable to this resource method.
   */
  FormParam[] formParams() default {};

  public static class NONE {}
}