package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Method extends HasStyles {

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

  boolean isHasParameterConstraints();

  boolean isHasParameterMultiplicity();

  Entity getRequestEntity();

  List<? extends StatusCode> getResponseCodes();

  Entity getResponseEntity();

  List<? extends StatusCode> getWarnings();

  List<? extends Parameter> getResponseHeaders();

  Set<String> getSecurityRoles();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
