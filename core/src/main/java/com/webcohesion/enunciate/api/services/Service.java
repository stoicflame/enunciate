package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Service extends HasStyles {

  String getLabel();

  String getPath();

  String getNamespace();

  ServiceGroup getGroup();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Operation> getOperations();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
