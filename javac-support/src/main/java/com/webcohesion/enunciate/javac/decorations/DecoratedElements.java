package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;

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

  public DecoratedElements(Elements delegate) {
    while (delegate instanceof DecoratedElements) {
      delegate = ((DecoratedElements) delegate).delegate;
    }
    this.delegate = delegate;
  }

  @Override
  public PackageElement getPackageElement(CharSequence name) {
    return delegate.getPackageElement(name);
  }

  @Override
  public TypeElement getTypeElement(CharSequence name) {
    return delegate.getTypeElement(name);
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

    return delegate.getPackageOf(e);
  }

  @Override
  public List<? extends Element> getAllMembers(TypeElement type) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    return delegate.getAllMembers(type);
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    while (e instanceof DecoratedElement) {
      e = ((DecoratedElement) e).getDelegate();
    }

    return delegate.getAllAnnotationMirrors(e);
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
