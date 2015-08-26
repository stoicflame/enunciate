package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Parameter;

/**
 * @author Ryan Heaton
 */
public class SwaggerParameter implements Parameter {

  private final Parameter delegate;
  private final String type;

  public SwaggerParameter(Parameter delegate, String type) {
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getDescription() {
    return delegate.getDescription();
  }

  @Override
  public String getDefaultValue() {
    return delegate.getDefaultValue();
  }

  @Override
  public String getTypeLabel() {
    return this.type;
  }
}
