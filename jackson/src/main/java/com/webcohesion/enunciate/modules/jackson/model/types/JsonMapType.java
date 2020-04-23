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
package com.webcohesion.enunciate.modules.jackson.model.types;

/**
 * The marker json type for a map.
 *
 * @author Ryan Heaton
 */
public class JsonMapType implements JsonType {

  private final JsonType keyType;
  private final JsonType valueType;

  public JsonMapType(JsonType keyType, JsonType valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /**
   * @return true
   */
  public boolean isObject() {
    return true;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isString() {
    return false;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isWholeNumber() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isMap() {
    return true;
  }

  /**
   * The json type of the key for the map.
   *
   * @return The json type of the key for the map.
   */
  public JsonType getKeyType() {
    return keyType;
  }

  /**
   * The json type of the value for the map.
   *
   * @return The json type of the value for the map.
   */
  public JsonType getValueType() {
    return valueType;
  }

  @Override
  public String getFormat() {
    return null;
  }
}
