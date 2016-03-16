package com.webcohesion.enunciate.api.resources;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Parameter {

  String getName();

  String getDescription();

  String getTypeLabel();

  String getDefaultValue();

  String getConstraints();

  Set<String> getConstraintValues();

  Map<String, AnnotationMirror> getAnnotations();

  boolean isMultivalued();
}
