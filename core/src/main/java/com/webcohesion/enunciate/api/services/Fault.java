package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Fault {

  String getName();

  String getConditions();

  DataTypeReference getDataType();

  Map<String, AnnotationMirror> getAnnotations();
}
