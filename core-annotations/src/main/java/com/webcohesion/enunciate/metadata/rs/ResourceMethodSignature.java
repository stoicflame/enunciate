/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.metadata.rs;

import jakarta.ws.rs.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to "override" the method signature of a REST method that doesn't conform to JAX-RS specification. Used for
 * purposes of leveraging the documentation features of Enunciate without requiring your methods to conform to JAX-RS.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.METHOD )
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