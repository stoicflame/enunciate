package com.webcohesion.enunciate.api.services;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Service {

  String getLabel();

  String getPath();

  String getNamespace();

  ServiceGroup getGroup();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Operation> getOperations();

  Map<String, AnnotationMirror> getAnnotations();
}
