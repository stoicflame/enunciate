package com.webcohesion.enunciate.api.resources;

import java.util.List;

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

  Entity getRequestEntity();

  List<? extends StatusCode> getResponseCodes();

  Entity getResponseEntity();

  List<? extends StatusCode> getWarnings();

  List<? extends Parameter> getResponseHeaders();
}
