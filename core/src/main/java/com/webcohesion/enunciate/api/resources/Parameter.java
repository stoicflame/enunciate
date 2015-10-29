package com.webcohesion.enunciate.api.resources;

/**
 * @author Ryan Heaton
 */
public interface Parameter {

  String getName();

  String getDescription();

  String getTypeLabel();

  String getDefaultValue();

  String getConstraints();
}
