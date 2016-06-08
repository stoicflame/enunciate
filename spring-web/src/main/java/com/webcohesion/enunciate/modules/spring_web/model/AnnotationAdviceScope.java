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
