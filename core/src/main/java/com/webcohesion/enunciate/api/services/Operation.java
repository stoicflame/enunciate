package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Operation extends HasStyles {

  String getName();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Parameter> getInputParameters();

  List<? extends Parameter> getOutputParameters();

  DataTypeReference getReturnType();

  String getReturnDescription();

  List<? extends Fault> getFaults();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
