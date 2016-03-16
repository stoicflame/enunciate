package com.webcohesion.enunciate.api.resources;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Method {

  Resource getResource();

  String getLabel();

  String getHttpMethod();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  boolean isIncludeDefaultParameterValues();

  List<? extends Parameter> getParameters();

  boolean isIncludeParameterConstraints();

  boolean isIncludeParameterMultiplicity();

  Entity getRequestEntity();

  List<? extends StatusCode> getResponseCodes();

  Entity getResponseEntity();

  List<? extends StatusCode> getWarnings();

  List<? extends Parameter> getResponseHeaders();

  Set<String> getSecurityRoles();

  Map<String, AnnotationMirror> getAnnotations();
}
