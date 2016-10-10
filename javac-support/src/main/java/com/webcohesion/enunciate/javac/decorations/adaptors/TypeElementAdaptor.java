package com.webcohesion.enunciate.javac.decorations.adaptors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface TypeElementAdaptor extends TypeElement, ElementAdaptor {

  Name getBinaryName();

  List<? extends Element> getAllMembers();

  boolean overrides(ExecutableElement overrider, ExecutableElement overridden);
}
