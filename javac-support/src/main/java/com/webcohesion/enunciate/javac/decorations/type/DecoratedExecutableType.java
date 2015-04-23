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

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.List;

/**
 * A decorated declared type provides a property for getting its actual type parameters as a list
 * (so they can be accessed with the [] operator in expression language).
 *
 * @author Ryan Heaton
 */
public class DecoratedExecutableType extends DecoratedTypeMirror<ExecutableType> implements ExecutableType {

  public DecoratedExecutableType(ExecutableType delegate, ProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public List<? extends TypeVariable> getTypeVariables() {
    return TypeMirrorDecorator.decorate(this.delegate.getTypeVariables(), env);
  }

  @Override
  public TypeMirror getReturnType() {
    return TypeMirrorDecorator.decorate(this.delegate.getReturnType(), env);
  }

  @Override
  public List<? extends TypeMirror> getParameterTypes() {
    return TypeMirrorDecorator.decorate(this.delegate.getParameterTypes(), env);
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    return TypeMirrorDecorator.decorate(this.delegate.getThrownTypes(), env);
  }
}
