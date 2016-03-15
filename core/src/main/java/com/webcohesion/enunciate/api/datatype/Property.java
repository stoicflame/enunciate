package com.webcohesion.enunciate.api.datatype;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Property {

  String getName();

  String getDescription();

  DataTypeReference getDataType();

  String getDeprecated();

  boolean isRequired();

  Map<String, AnnotationMirror> getAnnotations();
}
