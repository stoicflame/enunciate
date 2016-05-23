package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.PathSummary;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PathSummaryImpl implements PathSummary {

  private final String path;
  private final Set<String> methods;
  private final Set<String> styles;

  public PathSummaryImpl(String path, Set<String> methods, Set<String> styles) {
    this.path = path;
    this.methods = methods;
    this.styles = styles;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<String> getMethods() {
    return methods;
  }

  @Override
  public Set<String> getStyles() {
    return styles;
  }
}
