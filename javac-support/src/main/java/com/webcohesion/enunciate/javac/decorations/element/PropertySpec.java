package com.webcohesion.enunciate.javac.decorations.element;

/**
 * @author Ryan Heaton
 */
public interface PropertySpec {

  boolean isGetter(DecoratedExecutableElement executable);

  boolean isSetter(DecoratedExecutableElement executable);

  String getPropertyName(DecoratedExecutableElement method);

  boolean isPaired(DecoratedExecutableElement getter, DecoratedExecutableElement setter);
}
