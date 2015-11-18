package com.webcohesion.enunciate.modules.spring_web.model;

/**
 * @author Ryan Heaton
 */
public final class PathSegment {

  private final String prefix;
  private final String value;
  private final String variable;
  private final String regex;

  public PathSegment(String prefix, String value, String variable, String regex) {
    this.prefix = prefix;
    this.value = value;
    this.variable = variable;
    this.regex = regex;
  }

  public String getPrefix() {
    return prefix;
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
