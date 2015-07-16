package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Parameter;

/**
 * @author Ryan Heaton
 */
public class ResponseHeaderParameterImpl implements Parameter {

  private final String header;
  private final String description;

  public ResponseHeaderParameterImpl(String header, String description) {
    this.header = header;
    this.description = description;
  }

  @Override
  public String getName() {
    return this.header;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getTypeLabel() {
    return "header";
  }

  @Override
  public String getDefaultValue() {
    return null;
  }
}
