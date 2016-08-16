package com.webcohesion.enunciate.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Iterator;
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
    if (el.getAnnotation(NotNull.class) != null) {
      return true;
    }
    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        if (annotationElement.getAnnotation(NotNull.class) != null) {
          return true;
        }
        if (recurse && isNotNull(annotationElement, false)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean hasConstraints(Element el, boolean required) {
    if (required) {
      return true;
    }

    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        if (annotationElement != null) {
          Element pckg = annotationElement.getEnclosingElement();
          if (pckg instanceof PackageElement && ((PackageElement)pckg).getQualifiedName().toString().equals("javax.validation.constraints")) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static String describeConstraints(Element el, boolean required) {
    Null mustBeNull = el.getAnnotation(Null.class);
    if (mustBeNull != null) {
      return "must be null";
    }

    List<String> constraints = new ArrayList<String>();
    required = required || el.getAnnotation(NotNull.class) != null;
    if (required) {
      constraints.add("required");
    }

    AssertFalse mustBeFalse = el.getAnnotation(AssertFalse.class);
    if (mustBeFalse != null) {
      constraints.add("must be \"false\"");
    }

    AssertTrue mustBeTrue = el.getAnnotation(AssertTrue.class);
    if (mustBeTrue != null) {
      constraints.add("must be \"true\"");
    }

    DecimalMax decimalMax = el.getAnnotation(DecimalMax.class);
    if (decimalMax != null) {
      constraints.add("max: " + decimalMax.value() + (decimalMax.inclusive() ? "" : " (exclusive)"));
    }

    DecimalMin decimalMin = el.getAnnotation(DecimalMin.class);
    if (decimalMin != null) {
      constraints.add("min: " + decimalMin.value() + (decimalMin.inclusive() ? "" : " (exclusive)"));
    }

    Digits digits = el.getAnnotation(Digits.class);
    if (digits != null) {
      constraints.add("max digits: " + digits.integer() + " (integer), " + digits.fraction() + " (fraction)");
    }

    Future dateInFuture = el.getAnnotation(Future.class);
    if (dateInFuture != null) {
      constraints.add("future date");
    }

    Max max = el.getAnnotation(Max.class);
    if (max != null) {
      constraints.add("max: " + max.value());
    }

    Min min = el.getAnnotation(Min.class);
    if (min != null) {
      constraints.add("min: " + min.value());
    }

    Past dateInPast = el.getAnnotation(Past.class);
    if (dateInPast != null) {
      constraints.add("past date");
    }

    Pattern mustMatchPattern = el.getAnnotation(Pattern.class);
    if (mustMatchPattern != null) {
      constraints.add("regex: " + mustMatchPattern.regexp());
    }

    Size size = el.getAnnotation(Size.class);
    if (size != null) {
      constraints.add("max size: " + size.max() + ", min size: " + size.min());
    }

    if (constraints.isEmpty()) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    Iterator<String> it = constraints.iterator();
    while (it.hasNext()) {
      String token = it.next();
      builder.append(token);
      if (it.hasNext()) {
        builder.append(", ");
      }
    }
    return builder.toString();
  }
}
