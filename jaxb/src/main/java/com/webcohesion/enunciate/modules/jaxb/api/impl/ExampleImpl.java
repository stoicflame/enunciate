package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.Example;

/**
 * @author Ryan Heaton
 */
public abstract class ExampleImpl implements Example {
  @Override
  public String getLang() {
    return "xml";
  }

  @Override
  public abstract String getBody();
}
