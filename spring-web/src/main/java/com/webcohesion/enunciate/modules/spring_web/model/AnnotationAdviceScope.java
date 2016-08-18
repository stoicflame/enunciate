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
package com.webcohesion.enunciate.modules.spring_web.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class AnnotationAdviceScope implements AdviceScope {

  private final Set<String> annotations;

  public AnnotationAdviceScope(Set<String> annotations) {
    this.annotations = annotations;
  }

  @Override
  public boolean applies(Element el) {
    while (el != null && !(el instanceof TypeElement)) {
      el = el.getEnclosingElement();
    }

    if (el == null) {
      return false;
    }

    List<? extends AnnotationMirror> annotationMirrors = el.getAnnotationMirrors();
    if (annotationMirrors != null) {
      for (AnnotationMirror annotationMirror : annotationMirrors) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if (annotationType != null) {
          Element annotationElement = annotationType.asElement();
          if (annotationElement instanceof TypeElement && this.annotations.contains(((TypeElement) annotationElement).getQualifiedName().toString())) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
