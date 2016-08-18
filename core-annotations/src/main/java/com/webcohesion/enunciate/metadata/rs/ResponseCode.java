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

  /**
   * Any additional headers to expect when this response code is provided.
   *
   * @return Any additional headers to expect when this response code is provided.
   */
  ResponseHeader[] additionalHeaders() default {};

  /**
   * The expected representation when this response code is provided.
   *
   * @return The expected representation when this response code is provided.
   */
  TypeHint type() default @TypeHint;
}
