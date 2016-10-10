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
package com.webcohesion.enunciate.javac.decorations.type;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecoration;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

@SuppressWarnings ( "unchecked" )
public class DecoratedTypeMirror<T extends TypeMirror> implements TypeMirror {

  protected final T delegate;
  protected final DecoratedProcessingEnvironment env;
  private String docComment = "";

  public DecoratedTypeMirror(T delegate, DecoratedProcessingEnvironment env) {
    while (delegate instanceof DecoratedTypeMirror) {
      delegate = (T) ((DecoratedTypeMirror) delegate).delegate;
    }

    this.delegate = delegate;
    this.env = env;

    if (this.env.getTypeMirrorDecorations() != null) {
      for (TypeMirrorDecoration decoration : this.env.getTypeMirrorDecorations()) {
        decoration.applyTo(this);
      }
    }
  }

  @Override
  public TypeKind getKind() {
    return this.delegate.getKind();
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return this.delegate.accept(v, p);
  }

  public boolean equals(Object obj) {
    if (obj instanceof DecoratedTypeMirror) {
      return equals(((DecoratedTypeMirror) obj).delegate);
    }
    return delegate.equals(obj);
  }

  public String toString() {
    return delegate.toString();
  }

  public boolean isInstanceOf(Class<?> clazz) {
    return isInstanceOf(TypeMirrorUtils.mirrorOf(clazz, this.env));
  }

  public boolean isInstanceOf(String typeName) {
    return isInstanceOf(TypeMirrorUtils.mirrorOf(typeName, this.env));
  }

  public boolean isInstanceOf(Element type) {
    return type != null && isInstanceOf(type.asType());
  }

  public boolean isInstanceOf(TypeMirror candidate) {
    return candidate != null &&
      getKind() != TypeKind.EXECUTABLE &&
      getKind() != TypeKind.PACKAGE &&
      candidate.getKind() != TypeKind.EXECUTABLE &&
      candidate.getKind() != TypeKind.PACKAGE &&
      this.env.getTypeUtils().isAssignable(this.delegate, candidate);
  }

  public boolean isAnnotation() {
    return isDeclared() && ((DeclaredType)this.delegate).asElement().getKind() == ElementKind.ANNOTATION_TYPE;
  }

  public boolean isArray() {
    return getKind() == TypeKind.ARRAY;
  }

  public boolean isCollection() {
    return isInstanceOf(TypeMirrorUtils.collectionTypeErasure(this.env));
  }

  public boolean isList() {
    return isInstanceOf(TypeMirrorUtils.listTypeErasure(this.env));
  }

  public boolean isNull() {
    return getKind() == TypeKind.NULL;
  }

  public boolean isReferenceType() {
    return false;
  }
  public boolean isClass() {
    return isDeclared() && ((DeclaredType)this.delegate).asElement().getKind() == ElementKind.CLASS;
  }

  public boolean isDeclared() {
    return getKind() == TypeKind.DECLARED;
  }

  public boolean isEnum() {
    return isDeclared() && ((DeclaredType)this.delegate).asElement().getKind() == ElementKind.ENUM;
  }

  public boolean isInterface() {
    return isDeclared() && ((DeclaredType)this.delegate).asElement().getKind() == ElementKind.INTERFACE;
  }

  public boolean isPrimitive() {
    return getKind().isPrimitive();
  }

  public boolean isTypeVariable() {
    return getKind() == TypeKind.TYPEVAR;
  }

  public boolean isVoid() {
    return getKind() == TypeKind.VOID;
  }

  public boolean isWildcard() {
    return getKind() == TypeKind.WILDCARD;
  }

  public T getDelegate() {
    return this.delegate;
  }

  public String getDocComment() {
    return this.docComment;
  }

  public void setDocComment(String docComment) {
    this.docComment = docComment;
  }

  public String getDocValue() {
    String value = getDocComment();
    if (value != null) {
      value = value.trim();

      if ("".equals(value)) {
        value = null;
      }
    }
    return value;
  }

}
