package com.webcohesion.enunciate.util;

import org.reflections.util.FilterBuilder;

/**
* @author Ryan Heaton
*/
public final class AntPatternExclude extends FilterBuilder.Exclude {

  private final String pattern;

  public AntPatternExclude(String pattern) {
    super("-");
    this.pattern = pattern;
  }

  @Override
  public boolean apply(String input) {
    return !AntPatternMatcher.INSTANCE.match(this.pattern, input);
  }

  @Override
  public String toString() {
    return "-" + this.pattern;
  }
}
