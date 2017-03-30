package com.webcohesion.enunciate.modules.jaxws.model;

/**
 * @author Ryan Heaton
 */
public class HttpHeader {

  private final String name;
  private final String description;

  public HttpHeader(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

}
