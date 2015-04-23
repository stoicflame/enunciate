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
package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleElementVisitor6;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorates an {@link Element} when visited.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class ElementDecorator<E extends Element> extends SimpleElementVisitor6<E, Void> {

  private final ProcessingEnvironment env;

  private ElementDecorator(ProcessingEnvironment env) {
    this.env = env;
  }


  /**
   * Decorates a declaration.
   *
   * @param element The declaration to decorate.
   * @return The decorated declaration.
   */
  public static <E extends Element> E decorate(E element, ProcessingEnvironment env) {
    if (element == null) {
      return null;
    }

    ElementDecorator<E> decorator = new ElementDecorator<E>(env);
    return element.accept(decorator, null);
  }

  /**
   * Decorates a collection of elements.
   *
   * @param elements The elements to decorate.
   * @return The decorated elements.
   */
  @SuppressWarnings({"unchecked"})
  public static <E extends Element> List<E> decorate(List<E> elements, ProcessingEnvironment env) {
    if (elements == null) {
      return null;
    }

    ElementDecorator<E> decorator = new ElementDecorator<E>(env);
    ArrayList<E> decls = new ArrayList<E>(elements.size());
    for (E element : elements) {
      decls.add(element.accept(decorator, null));
    }
    return decls;
  }

  /**
   * Decorates a collection of annotation mirrors.
   *
   * @param annotationMirrors The annotation mirrors to decorate.
   * @param env The processing environment.
   * @return The collection of decorated annotation mirrors.
   */
  public static List<AnnotationMirror> decorateAnnotationMirrors(List<? extends AnnotationMirror> annotationMirrors, ProcessingEnvironment env) {
    if (annotationMirrors == null) {
      return null;
    }

    ArrayList<AnnotationMirror> mirrors = new ArrayList<AnnotationMirror>(annotationMirrors.size());
    for (AnnotationMirror annotationMirror : annotationMirrors) {
      if (!(annotationMirror instanceof DecoratedAnnotationMirror)) {
        annotationMirror = new DecoratedAnnotationMirror(annotationMirror, env);
      }

      mirrors.add(annotationMirror);
    }

    return mirrors;
  }

  @Override
  public E visitUnknown(Element e, Void aVoid) {
    return (E) e;
  }
}
