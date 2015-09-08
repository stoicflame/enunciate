package com.webcohesion.enunciate.api;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface PathSummary {

  String getPath();

  Set<String> getMethods();

}
