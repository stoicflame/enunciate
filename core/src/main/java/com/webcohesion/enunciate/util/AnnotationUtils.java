package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class AnnotationUtils {

  private AnnotationUtils() {}

  public static <A extends Annotation> List<A> getAnnotations(Class<A> clazz, Element el) {
    if (el == null || (el instanceof TypeElement && Object.class.getName().equals(((TypeElement) el).getQualifiedName().toString()))) {
      return Collections.emptyList();
    }

    ArrayList<A> allAnnotations = new ArrayList<A>();
    A annotation = el.getAnnotation(clazz);
    if (annotation != null) {
      allAnnotations.add(annotation);
    }

    allAnnotations.addAll(getAnnotations(clazz, el.getEnclosingElement()));

    if (el instanceof TypeElement) {
      //include the superclass.
      TypeMirror superclass = ((TypeElement) el).getSuperclass();
      if (superclass instanceof DeclaredType) {
        allAnnotations.addAll(getAnnotations(clazz, ((DeclaredType) superclass).asElement()));
      }
    }

    return allAnnotations;
  }

  public static List<JavaDoc.JavaDocTagList> getJavaDocTags(String tag, DecoratedElement el) {
    if (el == null || (el instanceof TypeElement && Object.class.getName().equals(((TypeElement) el).getQualifiedName().toString()))) {
      return Collections.emptyList();
    }

    ArrayList<JavaDoc.JavaDocTagList> allTags = new ArrayList<JavaDoc.JavaDocTagList>();
    JavaDoc.JavaDocTagList tagList = el.getJavaDoc().get(tag);
    if (tagList != null && !tagList.isEmpty()) {
      allTags.add(tagList);
    }

    allTags.addAll(getJavaDocTags(tag, (DecoratedElement) el.getEnclosingElement()));

    if (el instanceof TypeElement) {
      //include the superclass.
      TypeMirror superclass = ((TypeElement) el).getSuperclass();
      if (superclass instanceof DeclaredType) {
        allTags.addAll(getJavaDocTags(tag, (DecoratedElement) ((DeclaredType) superclass).asElement()));
      }
    }

    return allTags;
  }
}
