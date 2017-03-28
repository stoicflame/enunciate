package com.webcohesion.enunciate.javac.javadoc;

/**
 * @author Ryan Heaton
 */
public class StaticDocComment implements DocComment {

  private final String value;

  public StaticDocComment(String value) {
    if (value == null) {
      value = "";
    }
    this.value = value;
  }

  @Override
  public String get() {
    return this.value;
  }
}
