/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.*;

import javax.lang.model.element.*;
import javax.lang.model.util.SimpleElementVisitor14;
import javax.lang.model.util.SimpleElementVisitor8;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorates an {@link Element} when visited.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class ElementDecorator<E extends Element> extends SimpleElementVisitor14<E, Void> {

  private final DecoratedProcessingEnvironment env;

  private ElementDecorator(DecoratedProcessingEnvironment env) {
    this.env = env;
  }


  /**
   * Decorates a declaration.
   *
   * @param element The declaration to decorate.
   * @param env The processing environment.
   * @return The decorated declaration.
   */
  public static <E extends Element> E decorate(E element, DecoratedProcessingEnvironment env) {
    if (element == null) {
      return null;
    }

    if (element instanceof DecoratedElement) {
      return element;
    }

    ElementDecorator<E> decorator = new ElementDecorator<E>(env);
    return element.accept(decorator, null);
  }

  /**
   * Decorates a collection of elements.
   *
   * @param elements The elements to decorate.
   * @param env The decorated processing environment.
   * @return The decorated elements.
   */
  @SuppressWarnings ( {"unchecked"} )
  public static <E extends Element> List<E> decorate(List<E> elements, DecoratedProcessingEnvironment env) {
    if (elements == null) {
      return null;
    }

    ArrayList<E> decls = new ArrayList<E>(elements.size());
    for (E element : elements) {
      decls.add(decorate(element, env));
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
  public static List<AnnotationMirror> decorateAnnotationMirrors(List<? extends AnnotationMirror> annotationMirrors, DecoratedProcessingEnvironment env) {
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
  public E visitPackage(PackageElement e, Void nil) {
    return (E) new DecoratedPackageElement(e, this.env);
  }

  @Override
  public E visitType(TypeElement e, Void nil) {
    return (E) new DecoratedTypeElement(e, this.env);
  }

  @Override
  public E visitVariable(VariableElement e, Void nil) {
    return (E) new DecoratedVariableElement(e, this.env);
  }

  @Override
  public E visitExecutable(ExecutableElement e, Void nil) {
    return (E) new DecoratedExecutableElement(e, this.env);
  }

  @Override
  public E visitTypeParameter(TypeParameterElement e, Void nil) {
    return (E) new DecoratedTypeParameterElement(e, this.env);
  }

  @Override
  public E visitRecordComponent(RecordComponentElement e, Void nil) {
    return (E) new DecoratedRecordComponentElement(e, this.env);
  }

  @Override
  public E visitUnknown(Element e, Void nil) {
    //new, unknown element? just try to return a generic decoration for now.
    return (E) new DecoratedElement<>(e, this.env);
  }
}
