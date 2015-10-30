package com.webcohesion.enunciate.api.resources;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Parameter {

  String getName();

  String getDescription();

  String getTypeLabel();

  String getDefaultValue();

  String getConstraints();

  Map<String, AnnotationMirror> getAnnotations();
}
