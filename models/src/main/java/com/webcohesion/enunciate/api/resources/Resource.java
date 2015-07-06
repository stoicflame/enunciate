package com.webcohesion.enunciate.api.resources;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Resource {

  String getPath();

  String getSlug();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Method> getMethods();

}
