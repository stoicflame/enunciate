package com.webcohesion.enunciate.api;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public interface HasAnnotations {

  <A extends Annotation> A getAnnotation(Class<A> annotationType);

  Map<String, AnnotationMirror> getAnnotations();

}
