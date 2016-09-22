package com.webcohesion.enunciate.modules.lombok;

import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;

/**
 * @author Ryan Heaton
 */
public class LombokDecoration extends SimpleElementVisitor6<Void, Void> implements ElementDecoration {

  @Override
  public void applyTo(DecoratedElement e) {
    e.accept(this, null);
  }

  @Override
  public Void visitType(TypeElement e, Void aVoid) {
    DecoratedTypeElement typeElement = (DecoratedTypeElement) e;
    //todo: modify the element as needed.
    return null;
  }

}
