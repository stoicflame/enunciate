package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;

/**
 * @author Ryan Heaton
 */
public interface ElementDecoration {

  /**
   * Apply this decoration to the given decorated element.
   *
   * @param e The element to decorate.
   */
  void applyTo(DecoratedElement e);

}
