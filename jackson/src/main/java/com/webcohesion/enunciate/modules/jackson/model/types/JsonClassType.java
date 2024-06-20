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

import com.webcohesion.enunciate.modules.jackson.model.SimpleTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;

/**
 * Decorator for an json class type.
 *
 * @author Ryan Heaton
 */
public class JsonClassType implements JsonType {

  private final TypeDefinition typeDef;

  public JsonClassType(TypeDefinition typeDef) {
    if (typeDef == null) {
      throw new IllegalArgumentException("A type definition must be supplied.");
    }

    this.typeDef = typeDef;
  }

  public boolean isObject() {
    return !(this.typeDef instanceof SimpleTypeDefinition);
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isString() {
    return !isObject() && ((SimpleTypeDefinition) this.typeDef).getBaseType().isString();
  }

  @Override
  public boolean isNumber() {
    return !isObject() && ((SimpleTypeDefinition) this.typeDef).getBaseType().isNumber();
  }

  @Override
  public boolean isWholeNumber() {
    return isNumber() && ((SimpleTypeDefinition) this.typeDef).getBaseType().isWholeNumber();
  }

  @Override
  public boolean isBoolean() {
    return !isObject() && ((SimpleTypeDefinition) this.typeDef).getBaseType().isBoolean();
  }

  /**
   * Get the type definition for this class type.
   *
   * @return The type definition for this class type.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDef;
  }

  @Override
  public String getFormat() {
    return this.typeDef.getTypeFormat();
  }
}
