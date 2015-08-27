package com.webcohesion.enunciate.util;

import org.reflections.util.FilterBuilder;

/**
* @author Ryan Heaton
*/
public final class StringEqualsExclude extends FilterBuilder.Exclude {

  private final String string;

  public StringEqualsExclude(String string) {
    super("-");
    this.string = string;
  }

  @Override
  public boolean apply(String input) {
    return !input.equals(this.string);
  }

  @Override
  public String toString() {
    return "-" + this.string;
  }
}
