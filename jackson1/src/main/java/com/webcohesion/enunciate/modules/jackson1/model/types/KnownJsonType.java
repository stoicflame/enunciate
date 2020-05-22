/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jackson1.model.types;

/**
 * Set of known json types.
 *
 * @author Ryan Heaton
 */
public enum KnownJsonType implements JsonType {

  OBJECT(true, false, false, false, false, false, null),

  STRING(false, true, false, false, false, false, null),

  PASSWORD(false, true, false, false, false, false, "password"),

  DATE(false, true, false, false, false, false, "date"),

  DATE_TIME(false, true, false, false, false, false, "date-time"),

  NUMBER(false, false, true, false, false, false, null),

  WHOLE_NUMBER(false, false, true, true, false, false, "int32"),

  LONG_NUMBER(false, false, true, true, false, false, "int64"),

  BOOLEAN(false, false, false, false, true, false, null),

  ARRAY(false, false, false, false, false, true, null);

  private final boolean object;
  private final boolean string;
  private final boolean number;
  private final boolean whole;
  private final boolean bool;
  private final boolean array;
  private final String format;

  KnownJsonType(boolean object, boolean string, boolean number, boolean whole, boolean bool, boolean array, String format) {
    this.object = object;
    this.string = string;
    this.number = number;
    this.whole = whole;
    this.bool = bool;
    this.array = array;
    this.format = format;
  }

  @Override
  public boolean isObject() {
    return object;
  }

  @Override
  public boolean isString() {
    return string;
  }

  @Override
  public boolean isNumber() {
    return number;
  }

  @Override
  public boolean isWholeNumber() {
    return this.whole;
  }

  @Override
  public boolean isBoolean() {
    return bool;
  }

  @Override
  public boolean isArray() {
    return array;
  }

  @Override
  public String getFormat() {
    return this.format;
  }


}
