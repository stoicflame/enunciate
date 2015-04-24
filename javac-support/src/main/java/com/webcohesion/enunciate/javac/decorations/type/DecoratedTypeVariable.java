/*
 * Copyright 2006 Ryan Heaton
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
package com.webcohesion.enunciate.javac.decorations.type;

import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Ryan Heaton
 */
public class DecoratedTypeVariable extends DecoratedReferenceType<TypeVariable> implements TypeVariable {

  public DecoratedTypeVariable(TypeVariable delegate, ProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public Element asElement() {
    return ElementDecorator.decorate(this.delegate.asElement(), this.env);
  }

  @Override
  public TypeMirror getUpperBound() {
    return TypeMirrorDecorator.decorate(this.delegate.getUpperBound(), env);
  }

  @Override
  public TypeMirror getLowerBound() {
    return TypeMirrorDecorator.decorate(this.delegate.getLowerBound(), env);
  }

  public boolean isTypeVariable() {
    return true;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitTypeVariable(this, p);
  }
}
