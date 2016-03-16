package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface Entity {

  String getDescription();

  List<? extends MediaTypeDescriptor> getMediaTypes();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();
}
