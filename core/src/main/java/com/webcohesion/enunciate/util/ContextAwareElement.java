package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.EnunciateContext;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Ryan Heaton
 */
public class ContextAwareElement {

  private final EnunciateContext context;

  public ContextAwareElement(EnunciateContext context) {
    this.context = context;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public Types getTypeUtils() {
    return this.context.getTypeUtils();
  }

  public Elements getElementUtils() {
    return this.context.getElementUtils();
  }

  public boolean isInstanceOf(TypeMirror type, Class<?> clazz) {
    return context.isInstanceOf(type, clazz);
  }

  public boolean isInstanceOf(TypeMirror type, String fqn) {
    return context.isInstanceOf(type, fqn);
  }

  public boolean isInstanceOf(TypeMirror type, TypeElement typeElement) {
    return context.isInstanceOf(type, typeElement);
  }

  public boolean isInstanceOf(TypeMirror type, TypeMirror superClass) {
    return context.isInstanceOf(type, superClass);
  }
}
