package com.webcohesion.enunciate.javac.decorations.adaptors;

import com.webcohesion.enunciate.javac.decorations.SourcePosition;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface ElementAdaptor extends Element {

  String getDocComment();

  boolean isDeprecated();

  PackageElement getPackage();

  List<? extends AnnotationMirror> getAllAnnotationMirrors();

  boolean hides(Element hidden);

  boolean isHiddenBy(Element hider);

  SourcePosition getSourcePosition();
}
