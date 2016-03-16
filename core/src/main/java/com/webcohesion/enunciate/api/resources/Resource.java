package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Resource {

  String getPath();

  String getRelativePath();

  String getSlug();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Method> getMethods();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
