package com.webcohesion.enunciate.util;

import org.reflections.util.FilterBuilder;

/**
* @author Ryan Heaton
*/
public final class AntPatternInclude extends FilterBuilder.Include {

  private final String pattern;

  public AntPatternInclude(String pattern) {
    super("-");
    this.pattern = pattern;
  }

  @Override
  public boolean apply(String input) {
    return AntPatternMatcher.INSTANCE.match(this.pattern, input);
  }

  @Override
  public String toString() {
    return "+" + this.pattern;
  }
}
