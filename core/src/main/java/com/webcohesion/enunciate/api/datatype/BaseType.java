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
package com.webcohesion.enunciate.api.datatype;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Ryan Heaton
 */
public enum BaseType {

  bool,

  number,

  string,

  object;

  public static BaseType fromType(TypeMirror typeMirror) {
    if (typeMirror == null) {
      return null;
    }

    switch (typeMirror.getKind()) {
      case BOOLEAN:
        return BaseType.bool;
      case BYTE:
      case DOUBLE:
      case FLOAT:
      case INT:
      case LONG:
      case SHORT:
        return BaseType.number;
      case DECLARED:
        Element el = ((DeclaredType) typeMirror).asElement();
        if (el instanceof TypeElement && ((TypeElement) el).getQualifiedName().contentEquals(String.class.getName())) {
          return BaseType.string;
        }
      default:
        return BaseType.object;
    }
  }

}
