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
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.webcohesion.enunciate.javac.decorations.adaptors.ElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.metadata.Password;
import com.webcohesion.enunciate.metadata.json.JsonStringFormat;
import com.webcohesion.enunciate.metadata.rs.ResourceGroup;

/**
 * @author Ryan Heaton
 */
public class AnnotationUtils {

  private AnnotationUtils() {}

  public static <A extends Annotation> A getAnnotation(Class<A> clazz, Element el, boolean includeMetaAnnotations) {
    List<A> annotations = getAnnotations(clazz, el, includeMetaAnnotations);
    return annotations.isEmpty() ? null : annotations.get(0);
  }

  public static <A extends Annotation> List<A> getAnnotations(Class<A> clazz, Element el, boolean includeMetaAnnotations) {
    if (el == null || (el instanceof TypeElement && Object.class.getName().equals(((TypeElement) el).getQualifiedName().toString()))) {
      return Collections.emptyList();
    }

    ArrayList<A> allAnnotations = new ArrayList<A>();
    A annotation = includeMetaAnnotations ? getMetaAnnotation(clazz, el) : el.getAnnotation(clazz);
    if (annotation != null) {
      allAnnotations.add(annotation);
    }

    allAnnotations.addAll(getAnnotations(clazz, el.getEnclosingElement(), includeMetaAnnotations));

    if (el instanceof TypeElement) {
      //include the superclass.
      TypeMirror superclass = ((TypeElement) el).getSuperclass();
      if (superclass instanceof DeclaredType) {
        allAnnotations.addAll(getAnnotations(clazz, ((DeclaredType) superclass).asElement(), includeMetaAnnotations));
      }
    }

    return allAnnotations;
  }

  @Nullable
  public static <A extends Annotation> A getMetaAnnotation(Class<A> type, Element el) {
    return getMetaAnnotation(type, el, 0);
  }

  private static final int MAX_DEPTH = 2;

  @Nullable
  private static <A extends Annotation> A getMetaAnnotation(Class<A> type, Element el, int depth) {
    A result = el.getAnnotation(type);
    if (result != null) {
      return result;
    }
    for (AnnotationMirror annotation : el.getAnnotationMirrors()) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        result = annotationElement.getAnnotation(type);
        if (result != null) {
          return result;
        }
        if (depth < MAX_DEPTH) {
          result = getMetaAnnotation(type, annotationElement, depth + 1);
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
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

    if (el instanceof TypeElement) {
      //include the superclass.
      TypeMirror superclass = ((TypeElement) el).getSuperclass();
      if (superclass instanceof DeclaredType) {
        Element element = ((DeclaredType) superclass).asElement();
        allTags.addAll(getJavaDocTags(tag, element));
      }
    }

    allTags.addAll(getJavaDocTags(tag, el.getEnclosingElement()));

    return allTags;
  }

  public static ResourceGroup getResourceGroup(Element el) {
    ResourceGroup annotation = getAnnotation(ResourceGroup.class, el, true);
    if (annotation != null) {
      return annotation;
    }

    List<JavaDoc.JavaDocTagList> tags = getJavaDocTags("resourceGroup", el);

    if (!tags.isEmpty()) {
      return new ResourceGroup() {
        @Override
        public String value() {
          return tags.get(0).toString();
        }

        @Override
        public String description() {
          return "##default";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
          return null;
        }
      };
    }

    return null;
  }

  public static boolean isIgnored(Element element) {
    List<JavaDoc.JavaDocTagList> ignoreTags = getJavaDocTags("ignore", element);
    return !ignoreTags.isEmpty() || element.getAnnotation(Ignore.class) != null;
  }

  public static boolean isPassword(Element element) {
    List<JavaDoc.JavaDocTagList> ignoreTags = getJavaDocTags("password", element);
    return !ignoreTags.isEmpty() || element.getAnnotation(Password.class) != null;
  }

  public static String getJsonStringFormat(Element element) {
    List<JavaDoc.JavaDocTagList> formatTags = getJavaDocTags("jsonStringFormat", element);
    if (!formatTags.isEmpty()) {
      return formatTags.get(0).toString();
    }

    JsonStringFormat formatAnnotation = element.getAnnotation(JsonStringFormat.class);
    return formatAnnotation == null ? null : formatAnnotation.value();
  }
}
