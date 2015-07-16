package com.webcohesion.enunciate.api.resources;

import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface ResourceGroup {

  String getSlug();

  String getLabel();

  String getDescription();

  String getDeprecated();

  Set<String> getMethods();

  List<? extends Resource> getResources();

}
