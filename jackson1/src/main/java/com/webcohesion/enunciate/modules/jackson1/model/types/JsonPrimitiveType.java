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

package com.webcohesion.enunciate.modules.jackson1.model.types;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;

/**
 * @author Ryan Heaton
 */
public class JsonPrimitiveType implements JsonType {

  private final PrimitiveType type;

  public JsonPrimitiveType(PrimitiveType delegate) {
    this.type = delegate;
  }

  @Override
  public boolean isObject() {
    return false;
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
    switch (type.getKind()) {
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
      case BYTE: //todo: verify 'byte' serialization?
        return true;
    }
    return false;
  }

  @Override
  public boolean isWholeNumber() {
    switch (type.getKind()) {
      case INT:
      case LONG:
      case SHORT:
      case BYTE: //todo: verify 'byte' serialization?
        return true;
    }
    return false;
  }

  @Override
  public boolean isBoolean() {
    return this.type.getKind() == TypeKind.BOOLEAN;
  }
}
