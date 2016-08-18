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
package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class Styles {

  private Styles() {}

  /**
   * Gather facets.
   *
   * @param declaration The declaration on which to gather facets.
   * @return The facets gathered on the declaration.
   */
  public static Set<String> gatherStyles(Element declaration, Map<String, String> annotationStyles) {
    Set<String> bucket = new TreeSet<String>();
    if (declaration != null) {
      com.webcohesion.enunciate.metadata.Style style = declaration.getAnnotation(com.webcohesion.enunciate.metadata.Style.class);
      if (style != null) {
        bucket.add(style.value());
      }

      com.webcohesion.enunciate.metadata.Styles facets = declaration.getAnnotation(com.webcohesion.enunciate.metadata.Styles.class);
      if (facets != null) {
        for (com.webcohesion.enunciate.metadata.Style s : facets.value()) {
          bucket.add(s.value());
        }
      }

      List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
      for (AnnotationMirror annotationMirror : annotationMirrors) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if (annotationType != null) {
          Element annotationDeclaration = annotationType.asElement();
          if (annotationDeclaration instanceof TypeElement) {
            String configuredStyle = annotationStyles.get(((TypeElement) annotationDeclaration).getQualifiedName().toString());
            if (configuredStyle != null) {
              bucket.add(configuredStyle);
            }
          }

          style = annotationDeclaration.getAnnotation(com.webcohesion.enunciate.metadata.Style.class);
          if (style != null) {
            bucket.add(style.value());
          }
          facets = annotationDeclaration.getAnnotation(com.webcohesion.enunciate.metadata.Styles.class);
          if (facets != null) {
            for (com.webcohesion.enunciate.metadata.Style s : facets.value()) {
              bucket.add(s.value());
            }
          }
        }
      }

      if (declaration instanceof DecoratedElement) {
        JavaDoc.JavaDocTagList styleTags = ((DecoratedElement) declaration).getJavaDoc().get("style");
        if (styleTags != null) {
          for (String styleTag : styleTags) {
            bucket.add(styleTag);
          }
        }
      }
    }
    return bucket;
  }
}
