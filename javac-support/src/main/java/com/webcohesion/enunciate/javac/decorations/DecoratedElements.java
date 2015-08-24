package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class DecoratedElements implements Elements {

  private final Elements delegate;
  private final ProcessingEnvironment env;

  public DecoratedElements(Elements delegate, ProcessingEnvironment env) {
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

    return delegate.getDocComment(e);
  }

  @Override
  public boolean isDeprecated(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return delegate.isDeprecated(e);
  }

  @Override
  public Name getBinaryName(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return delegate.getBinaryName(type);
  }

  @Override
  public PackageElement getPackageOf(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return ElementDecorator.decorate(delegate.getPackageOf(e), this.env);
  }

  @Override
  public List<? extends Element> getAllMembers(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return ElementDecorator.decorate(delegate.getAllMembers(type), this.env);
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return ElementDecorator.decorateAnnotationMirrors(delegate.getAllAnnotationMirrors(e), this.env);
  }

  @Override
  public boolean hides(Element hider, Element hidden) {
    while (hider instanceof DecoratedElement) {
      hider = ((DecoratedElement) hider).getDelegate();
    }

    while (hidden instanceof DecoratedElement) {
      hidden = ((DecoratedElement) hidden).getDelegate();
    }

    return delegate.hides(hider, hidden);
  }

  @Override
  public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
    while (overrider instanceof DecoratedExecutableElement) {
      overrider = ((DecoratedExecutableElement) overrider).getDelegate();
    }

    while (overridden instanceof DecoratedExecutableElement) {
      overridden = ((DecoratedExecutableElement) overridden).getDelegate();
    }

    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
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

}
