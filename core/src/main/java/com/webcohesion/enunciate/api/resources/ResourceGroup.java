package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.PathSummary;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface ResourceGroup {

  String getSlug();

  String getLabel();

  String getSortKey();

  String getDescription();

  String getDeprecated();

  List<PathSummary> getPaths();

  String getContextPath();

  List<Resource> getResources();

}
