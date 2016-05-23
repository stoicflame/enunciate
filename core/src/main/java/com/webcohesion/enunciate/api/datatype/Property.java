package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Property extends HasStyles {

  String getName();

  String getDescription();

  DataTypeReference getDataType();

  String getDeprecated();

  boolean isRequired();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
