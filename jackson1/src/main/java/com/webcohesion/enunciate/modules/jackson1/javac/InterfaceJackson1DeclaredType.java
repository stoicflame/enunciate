package com.webcohesion.enunciate.modules.jackson1.javac;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

/**
 * @author Ryan Heaton
 */
public class InterfaceJackson1DeclaredType extends DecoratedDeclaredType {

  private final InterfaceJackson1TypeElement element;

  public InterfaceJackson1DeclaredType(DeclaredType root, DecoratedProcessingEnvironment env) {
    super(root, env);
    this.element = new InterfaceJackson1TypeElement(root, env);
  }

  @Override
  public Element asElement() {
    return this.element;
  }

}
