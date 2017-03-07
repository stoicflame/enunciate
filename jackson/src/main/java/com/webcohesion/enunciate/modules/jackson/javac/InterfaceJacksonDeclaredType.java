package com.webcohesion.enunciate.modules.jackson.javac;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

/**
 * @author Ryan Heaton
 */
public class InterfaceJacksonDeclaredType extends DecoratedDeclaredType {

  private final InterfaceJacksonTypeElement element;

  public InterfaceJacksonDeclaredType(DeclaredType root, DecoratedProcessingEnvironment env) {
    super(root, env);
    this.element = new InterfaceJacksonTypeElement(root, env);
  }

  @Override
  public Element asElement() {
    return this.element;
  }

}
