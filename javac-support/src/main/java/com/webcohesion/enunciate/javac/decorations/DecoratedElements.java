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
package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.RecordCompatibility;
import com.webcohesion.enunciate.javac.decorations.adaptors.ElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.adaptors.ExecutableElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.adaptors.TypeElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.ParamDocComment;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class DecoratedElements implements Elements {

  private final Elements delegate;
  private final DecoratedProcessingEnvironment env;

  public DecoratedElements(Elements delegate, DecoratedProcessingEnvironment env) {
    this.env = env;
    while (delegate instanceof DecoratedElements) {
      delegate = ((DecoratedElements) delegate).delegate;
    }
    this.delegate = delegate;
  }

  @Override
  public PackageElement getPackageElement(CharSequence name) {
    return ElementDecorator.decorate(delegate.getPackageElement(name), this.env);
  }

  @Override
  public TypeElement getTypeElement(CharSequence name) {
    return ElementDecorator.decorate(delegate.getTypeElement(name), this.env);
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
    while (a instanceof DecoratedAnnotationMirror) {
      a = ((DecoratedAnnotationMirror)a).getDelegate();
    }

    return delegate.getElementValuesWithDefaults(a);
  }

  @Override
  public String getDocComment(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    String recordComponentName = null;
    if (RecordCompatibility.isRecordComponent(e)) {
      recordComponentName = e.getSimpleName().toString();
      e = e.getEnclosingElement();
    }

    String docComment;
    if (e instanceof ElementAdaptor) {
      docComment = ((ElementAdaptor) e).getDocComment();
    }
    else {
      docComment = delegate.getDocComment(e);
    }

    if (recordComponentName != null) {
      JavaDoc recordDoc = new JavaDoc(docComment, null, null, this.env);
      HashMap<String, String> params = ParamDocComment.loadParamsComments("param", recordDoc);
      docComment = params.get(recordComponentName);
    }

    return docComment;
  }

  @Override
  public boolean isDeprecated(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return e instanceof ElementAdaptor ? ((ElementAdaptor)e).isDeprecated() : delegate.isDeprecated(e);
  }

  @Override
  public Name getBinaryName(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return type instanceof TypeElementAdaptor ? ((TypeElementAdaptor)type).getBinaryName() : delegate.getBinaryName(type);
  }

  @Override
  public PackageElement getPackageOf(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return e instanceof ElementAdaptor ? ((ElementAdaptor)e).getPackage() : ElementDecorator.decorate(delegate.getPackageOf(e), this.env);
  }

  @Override
  public List<? extends Element> getAllMembers(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return type instanceof TypeElementAdaptor ? ((TypeElementAdaptor)type).getAllMembers() : ElementDecorator.decorate(delegate.getAllMembers(type), this.env);
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return e instanceof ElementAdaptor ? ((ElementAdaptor)e).getAllAnnotationMirrors() : ElementDecorator.decorateAnnotationMirrors(delegate.getAllAnnotationMirrors(e), this.env);
  }

  @Override
  public boolean hides(Element hider, Element hidden) {
    while (hider instanceof DecoratedElement) {
      hider = ((DecoratedElement) hider).getDelegate();
    }

    while (hidden instanceof DecoratedElement) {
      hidden = ((DecoratedElement) hidden).getDelegate();
    }

    if (hider instanceof ElementAdaptor) {
      return ((ElementAdaptor)hider).hides(hidden);
    }

    if (hidden instanceof ElementAdaptor) {
      return ((ElementAdaptor)hidden).isHiddenBy(hider);
    }

    return delegate.hides(hider, hidden);
  }

  @Override
  public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
    while (overrider instanceof DecoratedExecutableElement) {
      overrider = ((DecoratedExecutableElement) overrider).getDelegate();
    }

    if (overrider instanceof ExecutableElementAdaptor) {
      return ((ExecutableElementAdaptor)overrider).overrides(overridden, type);
    }

    while (overridden instanceof DecoratedExecutableElement) {
      overridden = ((DecoratedExecutableElement) overridden).getDelegate();
    }

    if (overridden instanceof ExecutableElementAdaptor) {
      return ((ExecutableElementAdaptor)overridden).isOverriddenBy(overrider, type);
    }

    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    if (type instanceof TypeElementAdaptor) {
      return ((TypeElementAdaptor)type).overrides(overrider, overridden);
    }

    return delegate.overrides(overrider, overridden, type);
  }

  @Override
  public String getConstantExpression(Object value) {
    return delegate.getConstantExpression(value);
  }

  @Override
  public void printElements(Writer w, Element... elements) {
    Element[] copy = new Element[elements.length];
    for (int i = 0; i < elements.length; i++) {
      Element e = elements[i];
      while (e instanceof DecoratedElement) {
        e = ((DecoratedElement) e).getDelegate();
      }

      copy[i] = e;
    }

    delegate.printElements(w, copy);
  }

  @Override
  public Name getName(CharSequence cs) {
    return delegate.getName(cs);
  }

  @Override
  public boolean isFunctionalInterface(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return delegate.isFunctionalInterface(type);
  }
}
