package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.javac.decorations.AnnotationMirrorDecoration;
import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecoration;

import java.util.List;

/**
 * Marker interface for designating a module as one that modifies the Enunciate context.
 *
 * @author Ryan Heaton
 */
public interface ContextModifyingModule extends EnunciateModule {

  List<ElementDecoration> getElementDecorations();

  List<TypeMirrorDecoration> getTypeMirrorDecorations();

  List<AnnotationMirrorDecoration> getAnnotationMirrorDecorations();

}
