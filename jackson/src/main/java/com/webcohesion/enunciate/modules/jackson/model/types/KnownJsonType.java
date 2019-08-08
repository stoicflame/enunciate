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

import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;

/**
 * Set of known json types.
 *
 * @author Ryan Heaton
 */
public enum KnownJsonType implements JsonType {

  OBJECT(true, false, false, false, false, false, null),

  STRING(false, true, false, false, false, false, null),

  DATE_STRING(false, true, false, false, false, false, BaseTypeFormat.DATE_TIME),

  NUMBER(false, false, true, false, false, false, null),

  WHOLE_NUMBER(false, false, true, true, false, false, BaseTypeFormat.INT32),

  LONG_NUMBER(false, false, true, true, false, false, BaseTypeFormat.INT64),

  BOOLEAN(false, false, false, false, true, false, null),

  ARRAY(false, false, false, false, false, true, null);

  private final boolean object;
  private final boolean string;
  private final boolean number;
  private final boolean whole;
  private final boolean bool;
  private final boolean array;
  private final BaseTypeFormat format;

  KnownJsonType(boolean object, boolean string, boolean number, boolean whole, boolean bool, boolean array, BaseTypeFormat format) {
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
    return whole;
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
  public BaseTypeFormat getFormat() {
    return this.format;
  }


}
