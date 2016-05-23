package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Fault extends HasStyles {

  String getName();

  String getConditions();

  DataTypeReference getDataType();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
