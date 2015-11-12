package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.PathSummary;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PathSummaryImpl implements PathSummary {

  private final String path;
  private final Set<String> methods;

  public PathSummaryImpl(String path, Set<String> methods) {
    this.path = path;
    this.methods = methods;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<String> getMethods() {
    return methods;
  }
}
