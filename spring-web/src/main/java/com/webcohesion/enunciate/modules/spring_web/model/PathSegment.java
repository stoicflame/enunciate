package com.webcohesion.enunciate.modules.spring_web.model;

/**
 * @author Ryan Heaton
 */
public final class PathSegment {

  private final String value;
  private final String variable;
  private final String regex;

  public PathSegment(String value, String variable, String regex) {
    this.value = value;
    this.variable = variable;
    this.regex = regex;
  }

  public String getValue() {
    return value;
  }

  public String getVariable() {
    return variable;
  }

  public String getRegex() {
    return regex;
  }
}
