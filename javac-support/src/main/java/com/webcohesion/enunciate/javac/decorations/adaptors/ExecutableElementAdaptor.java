package com.webcohesion.enunciate.javac.decorations.adaptors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author Ryan Heaton
 */
public interface ExecutableElementAdaptor extends ExecutableElement, ElementAdaptor {

  boolean overrides(ExecutableElement overridden, TypeElement scope);

  boolean isOverriddenBy(ExecutableElement overrider, TypeElement type);
}
