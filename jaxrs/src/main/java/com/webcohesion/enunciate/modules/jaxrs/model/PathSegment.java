package com.webcohesion.enunciate.modules.jaxrs.model;

/**
 * @author Ryan Heaton
 */
public final class PathSegment {

  private final String value;
  private final String regex;

  public PathSegment(String value, String regex) {
    this.value = value;
    this.regex = regex;
  }

  public String getValue() {
    return value;
  }

  public String getRegex() {
    return regex;
  }
}
