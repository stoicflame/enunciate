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
package com.webcohesion.enunciate.modules.jackson.model.types;

/**
 * @author Ryan Heaton
 */
public interface JsonType {

  /**
   * Whether this is an object JSON type.
   *
   * @return Whether this is an object JSON type.
   */
  boolean isObject();

  /**
   * Whether this is an array JSON type.
   *
   * @return Whether this is an array JSON type.
   */
  boolean isArray();

  /**
   * Whether this is a string JSON type.
   *
   * @return Whether this is a string JSON type.
   */
  boolean isString();

  /**
   * Whether this is a number JSON type.
   *
   * @return Whether this is a number JSON type.
   */
  boolean isNumber();

  /**
   * Whether this is a whole number.
   *
   * @return Whether this is a whole number.
   */
  boolean isWholeNumber();

  /**
   * Whether this is a boolean JSON type.
   *
   * @return Whether this is a boolean JSON type.
   */
  boolean isBoolean();

}
