package com.webcohesion.enunciate.api.services;

import java.util.List;

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
}
