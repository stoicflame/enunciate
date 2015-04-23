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
package com.webcohesion.enunciate.javac.decorations.element;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class DecoratedAnnotationMirror implements AnnotationMirror {

  private final AnnotationMirror delegate;
  private final ProcessingEnvironment env;

  public DecoratedAnnotationMirror(AnnotationMirror delegate, ProcessingEnvironment env) {
    if (delegate == null) {
      throw new NullPointerException("A delegate must be provided.");
    }

    if (env == null) {
      throw new NullPointerException("A processing environment must be provided.");
    }

    //unwrap.
    while (delegate instanceof DecoratedAnnotationMirror) {
      delegate = ((DecoratedAnnotationMirror) delegate).delegate;
    }

    this.delegate = delegate;
    this.env = env;
  }

  public DeclaredType getAnnotationType() {
    return this.delegate.getAnnotationType();
  }

  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
    return this.delegate.getElementValues();
  }

  public Map<? extends ExecutableElement, ? extends AnnotationValue> getAllElementValues() {
    return this.env.getElementUtils().getElementValuesWithDefaults(this.delegate);
  }

  public boolean equals(Object o) {
    if (o instanceof DecoratedAnnotationMirror) {
      o = ((DecoratedAnnotationMirror) o).delegate;
    }
    return this.delegate.equals(o);
  }

  public AnnotationMirror getDelegate() {
    return delegate;
  }
}
