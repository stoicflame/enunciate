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
package com.webcohesion.enunciate.metadata;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to mark a method as an example method to be used in the generated documentation.
 *
 * @author Ryan Heaton
 */
@Target ( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface DocumentationExample {

  /**
   * Whether to exclude this example.
   *
   * @return Whether to exclude this example.
   */
  boolean exclude() default false;

  /**
   * The value of this documentation example.
   *
   * @return The value of this documentation example.
   */
  String value() default "##default";

  /**
   * A second value for this documentation example to be used e.g. for arrays.
   *
   * @return A second value for this documentation example to be used e.g. for arrays.
   */
  String value2() default "##default";

  /**
   * The type to use for documentation purposes, useful (for example) when a specific subclass is desired in the documentation example.
   *
   * @return The type to use for documentation purposes, useful (for example) when a specific subclass is desired in the documentation example.
   */
  TypeHint type() default @TypeHint;

  /**
   * The value used to override entire JSON example.
   *
   * @return The value used to override entire JSON example.
   */
  String jsonOverride() default "##default";

}
