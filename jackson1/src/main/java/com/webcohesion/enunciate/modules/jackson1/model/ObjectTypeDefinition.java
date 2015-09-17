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

package com.webcohesion.enunciate.modules.jackson1.model;

import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * A type definition for a json type.
 *
 * @author Ryan Heaton
 */
public class ObjectTypeDefinition extends TypeDefinition {

  public ObjectTypeDefinition(TypeElement delegate, EnunciateJackson1Context context) {
    super(delegate, context);
  }

  public JsonType getSupertype() {
    TypeMirror superclass = getSuperclass();
    if (superclass == null || superclass.getKind() == TypeKind.NONE) {
      return null;
    }
    else if (superclass instanceof DeclaredType && ((TypeElement)((DeclaredType)superclass).asElement()).getQualifiedName().toString().equals(Object.class.getName())) {
      return null;
    }
    else {
      return JsonTypeFactory.getJsonType(superclass, this.context);
    }
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isObject() {
    return true;
  }

  @Override
  public boolean isBaseObject() {
    TypeMirror superclass = getSuperclass();
    if (superclass == null || superclass.getKind() == TypeKind.NONE) {
      return true;
    }

    TypeElement superDeclaration = (TypeElement) this.env.getTypeUtils().asElement(superclass);
    return superDeclaration == null
      || Object.class.getName().equals(superDeclaration.getQualifiedName().toString())
      || isJsonIgnored(superDeclaration);
  }

}
