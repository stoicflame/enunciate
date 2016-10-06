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
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.util.List;

/**
 * A decorated declared type provides a property for getting its actual type parameters as a list
 * (so they can be accessed with the [] operator in expression language).
 *
 * @author Ryan Heaton
 */
public class DecoratedDeclaredType extends DecoratedReferenceType<DeclaredType> implements DeclaredType {

  public DecoratedDeclaredType(DeclaredType delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public Element asElement() {
    return ElementDecorator.decorate(this.delegate.asElement(), this.env);
  }

  @Override
  public TypeMirror getEnclosingType() {
    return TypeMirrorDecorator.decorate(this.delegate.getEnclosingType(), env);
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return TypeMirrorDecorator.decorate(this.delegate.getTypeArguments(), env);
  }

  public boolean isDeclared() {
    return true;
  }

  public String getQualifiedName() {
    TypeElement element = (TypeElement) this.delegate.asElement();
    if (element != null) {
      return element.getQualifiedName().toString();
    }
    else {
      return "";
    }
  }

  @Override
  public boolean isInstanceOf(Class<?> clazz) {
    //qualified name check is a performance optimization.
    return getQualifiedName().equals(clazz.getName()) || super.isInstanceOf(clazz);
  }

  public boolean isInstanceOf(String typeName) {
    //qualified name check is a performance optimization.
    return getQualifiedName().equals(typeName) || super.isInstanceOf(typeName);
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(this, p);
  }
}
