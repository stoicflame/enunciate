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
    return !this.typeDef.isSimple() && !this.typeDef.isEnum();
  }

  @Override
  public boolean isArray() {
    return this.typeDef.getBaseType().isArray();
  }

  @Override
  public boolean isString() {
    return this.typeDef.getBaseType().isString();
  }

  @Override
  public boolean isNumber() {
    return this.typeDef.getBaseType().isNumber();
  }

  @Override
  public boolean isBoolean() {
    return this.typeDef.getBaseType().isBoolean();
  }

  /**
   * Get the type definition for this class type.
   *
   * @return The type definition for this class type.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDef;
  }

}
