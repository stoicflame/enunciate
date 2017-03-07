package com.webcohesion.enunciate.modules.jackson1.api.impl;

/**
 * @author Ryan Heaton
 */
public class CustomExampleImpl extends ExampleImpl {

  private final String body;

  public CustomExampleImpl(String body) {
    this.body = body;
  }

  @Override
  public String getBody() {
    return body;
  }
}
