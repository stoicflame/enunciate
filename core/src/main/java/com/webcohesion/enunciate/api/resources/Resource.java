package com.webcohesion.enunciate.api.resources;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

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

  Map<String, AnnotationMirror> getAnnotations();
}
