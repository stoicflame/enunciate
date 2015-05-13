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

package com.webcohesion.enunciate.modules.jackson.model.types;

/**
 * Set of known json types.
 *
 * @author Ryan Heaton
 */
public enum KnownJsonType implements JsonType {

  OBJECT(true, false, false, false, false),

  STRING(false, true, false, false, false),

  NUMBER(false, false, true, false, false),

  BOOLEAN(false, false, false, true, false),

  ARRAY(false, false, false, false, true);

  private final boolean object;
  private final boolean string;
  private final boolean number;
  private final boolean bool;
  private final boolean array;

  KnownJsonType(boolean object, boolean string, boolean number, boolean bool, boolean array) {
    this.object = object;
    this.string = string;
    this.number = number;
    this.bool = bool;
    this.array = array;
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
  public boolean isBoolean() {
    return bool;
  }

  @Override
  public boolean isArray() {
    return array;
  }
}
