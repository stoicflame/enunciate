/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Used to specify the content type(s) available for a REST resource.
 *
 * @author Ryan Heaton
 */
@Retention (
  RetentionPolicy.RUNTIME
)
@Target (
  { ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE }
)
public @interface ContentType {

  /**
   * The supported content types.
   *
   * @return The supported content types.
   */
  String[] value() default {};

  /**
   * The charset.
   *
   * @return The charset.
   */
  String charset() default "utf-8";

  /**
   * The unsupported content types.
   *
   * @return The unsupported content types.
   */
  String[] unsupported() default {};

  /**
   * The default content type to use.
   *
   * @return The default content type to use.
   */
  String defaultContentType() default "##undefined";
}
