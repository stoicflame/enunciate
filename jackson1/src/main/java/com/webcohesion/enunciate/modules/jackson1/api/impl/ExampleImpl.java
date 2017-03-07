package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.datatype.Example;

/**
 * @author Ryan Heaton
 */
public abstract class ExampleImpl implements Example {
  @Override
  public String getLang() {
    return "js";
  }

  @Override
  public abstract String getBody();
}
