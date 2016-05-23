package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Parameter extends HasStyles {

  String getName();

  String getDescription();

  DataTypeReference getDataType();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
