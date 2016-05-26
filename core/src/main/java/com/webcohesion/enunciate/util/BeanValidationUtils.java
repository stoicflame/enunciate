package com.webcohesion.enunciate.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class BeanValidationUtils {

  private BeanValidationUtils() {}

  public static boolean isNotNull(Element el) {
    return isNotNull(el, true);
  }

  private static boolean isNotNull(Element el, boolean recurse) {
    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        if (annotationElement instanceof TypeElement && "javax.validation.constraints.NotNull".equals(((TypeElement) annotationElement).getQualifiedName().toString())) {
          return true;
        }

        if (recurse) {
          return isNotNull(annotationElement, false);
        }
      }
    }

    return false;
  }
}
