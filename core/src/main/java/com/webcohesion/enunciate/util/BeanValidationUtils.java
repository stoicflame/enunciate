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

import com.webcohesion.enunciate.metadata.ReadOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class BeanValidationUtils {

  private static final Set<String> OPTIONALS;
  private BeanValidationUtils() {}

  static {
      HashSet<String> opts = new HashSet<>();
      opts.add(Optional.class.getName());
      opts.add(OptionalInt.class.getName());
      opts.add(OptionalLong.class.getName());
      opts.add(OptionalDouble.class.getName());
      OPTIONALS = Collections.unmodifiableSet(opts);
  }

  public static boolean isNotNull(Element el) {
    return isNotNull(el, true);
  }

  private static boolean isNotNull(Element el, boolean recurse) {
    final TypeMirror asType = el.asType();
    if (asType instanceof PrimitiveType) {
      return true;
    }
    if (asType instanceof DeclaredType &&
        OPTIONALS.stream().anyMatch(p ->
            ((QualifiedNameable)((DeclaredType) asType).asElement()).getQualifiedName().contentEquals(p)))
    {
      return false;
    }
    if (el.getAnnotation(NotNull.class) != null || el.getAnnotation(Nonnull.class) != null) {
      return true;
    }
    if (el.getAnnotation(Nullable.class) != null) {
      return false;
    }
    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        try {
          if (annotationElement.getAnnotation(NotNull.class) != null) {
            return true;
          }
          if (recurse && isNotNull(annotationElement, false)) {
            return true;
          }
        } catch (Exception e) {
          //See https://github.com/stoicflame/enunciate/issues/872; recursing sometimes encounters types not on the classpath.
          return false;
        }
      }
    }

    return notNullByDefault(el);
  }

  private static boolean notNullByDefault(Element el) {
    if (el == null) {
      return false;
    }
    if (el.getAnnotation(ParametersAreNonnullByDefault.class) != null) {
      return true;
    }
    return notNullByDefault(el.getEnclosingElement());
  }

  public static boolean hasConstraints(Element el, boolean required) {
    if (required) {
      return true;
    }

    if (el.getAnnotation(ReadOnly.class) != null) {
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
    required = required || isNotNull(el);
    if (required) {
      constraints.add("required");
    }

    ReadOnly readOnly = el.getAnnotation(ReadOnly.class);
    if (readOnly != null) {
      constraints.add("read-only");
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
