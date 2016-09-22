package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

/**
 * @author Ryan Heaton
 */
public interface TypeMirrorDecoration {

  /**
   * Apply this decoration to the given decorated type mirror.
   *
   * @param mirror The mirror to decorate.
   */
  void applyTo(DecoratedTypeMirror mirror);

}
