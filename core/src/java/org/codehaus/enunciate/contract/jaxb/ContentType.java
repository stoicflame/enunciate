/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb;

/**
 * Enum for content type of a complex type def.
 *
 * @author Ryan Heaton
 */
public enum ContentType {

  /**
   * Empty content type.
   */
  EMPTY,

  /**
   * Mixed content type.
   */
  MIXED,

  /**
   * Simple content type.
   */
  SIMPLE,

  /**
   * Complex (element-only) content type.
   */
  COMPLEX,

  /**
   * Implied complex content type (i.e. complex content that restricts the distinguished ur-type definition).
   */
  IMPLIED;

  /**
   * Whether this is the empty content type.
   *
   * @return Whether this is the empty content type.
   */
  public boolean isEmpty() {
    return this == EMPTY;
  }

  /**
   * Whether this is the mixed content type.
   *
   * @return Whether this is the mixed content type.
   */
  public boolean isMixed() {
    return this == MIXED;
  }

  /**
   * Whether this is the simple content type.
   *
   * @return Whether this is the simple content type.
   */
  public boolean isSimple() {
    return this == SIMPLE;
  }

  /**
   * Whether this is the complex content type.
   *
   * @return Whether this is the complex content type.
   */
  public boolean isComplex() {
    return ((this == COMPLEX) || (this == EMPTY) || (this == IMPLIED));
  }

  /**
   * Whether this is the implied content type.
   *
   * @return Whether this is the implied content type.
   */
  public boolean isImplied() {
    return this == IMPLIED;
  }
}
