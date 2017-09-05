/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.webcohesion.enunciate.javac.decorations.adaptors.ElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

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

  public static List<JavaDoc.JavaDocTagList> getJavaDocTags(String tag, Element el) {
    if (el == null || (el instanceof TypeElement && Object.class.getName().equals(((TypeElement) el).getQualifiedName().toString()))) {
      return Collections.emptyList();
    }

    ArrayList<JavaDoc.JavaDocTagList> allTags = new ArrayList<JavaDoc.JavaDocTagList>();
    JavaDoc.JavaDocTagList tagList = null;

    if (el instanceof ElementAdaptor) {
      tagList = new JavaDoc(((ElementAdaptor)el).getDocComment(), null, null, null).get(tag);
    }
    else if (el instanceof DecoratedElement) {
      tagList = new JavaDoc(((DecoratedElement)el).getDocComment(), null, null, null).get(tag);
    }

    if (tagList != null && !tagList.isEmpty()) {
      allTags.add(tagList);
    }

    allTags.addAll(getJavaDocTags(tag, el.getEnclosingElement()));

    if (el instanceof TypeElement) {
      //include the superclass.
      TypeMirror superclass = ((TypeElement) el).getSuperclass();
      if (superclass instanceof DeclaredType) {
        Element element = ((DeclaredType) superclass).asElement();
        allTags.addAll(getJavaDocTags(tag, element));
      }
    }

    return allTags;
  }
}
