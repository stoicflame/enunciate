package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Parameter extends HasStyles {

  String getName();

  String getDescription();

  String getTypeLabel();

  String getTypeName();

  String getDefaultValue();

  String getConstraints();

  Set<String> getConstraintValues();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();

  boolean isMultivalued();
}
