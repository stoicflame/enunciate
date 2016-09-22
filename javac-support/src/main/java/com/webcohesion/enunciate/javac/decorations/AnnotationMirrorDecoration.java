package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;

/**
 * @author Ryan Heaton
 */
public interface AnnotationMirrorDecoration {

  /**
   * Apply this decoration to the given decorated annotation mirror.
   *
   * @param mirror The mirror to decorate.
   */
  void applyTo(DecoratedAnnotationMirror mirror);

}
