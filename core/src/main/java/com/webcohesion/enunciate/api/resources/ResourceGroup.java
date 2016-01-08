package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.PathSummary;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

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

  String getRelativeContextPath();

  List<Resource> getResources();

  Map<String, AnnotationMirror> getAnnotations();
}
