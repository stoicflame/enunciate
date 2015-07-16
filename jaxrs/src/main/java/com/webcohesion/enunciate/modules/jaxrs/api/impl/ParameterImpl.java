package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceParameter;

/**
 * @author Ryan Heaton
 */
public class ParameterImpl implements Parameter {

  private final ResourceParameter param;

  public ParameterImpl(ResourceParameter param) {
    this.param = param;
  }

  @Override
  public String getName() {
    return this.param.getParameterName();
  }

  @Override
  public String getDescription() {
    return this.param.getJavaDoc().toString();
  }

  @Override
  public String getTypeLabel() {
    return this.param.getTypeName();
  }

  @Override
  public String getDefaultValue() {
    return this.param.getDefaultValue();
  }
}
