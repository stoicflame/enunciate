package com.webcohesion.enunciate.api;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface PathSummary extends HasStyles {

  String getPath();

  Set<String> getMethods();

}
