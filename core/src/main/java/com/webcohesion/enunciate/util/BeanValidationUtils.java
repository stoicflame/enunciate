/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.AllowedValues;
import com.webcohesion.enunciate.metadata.ReadOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ryan Heaton
 */
public class BeanValidationUtils {

  private static final Set<String> OPTIONALS;

  private BeanValidationUtils() {
  }

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
          ((QualifiedNameable) ((DeclaredType) asType).asElement()).getQualifiedName().contentEquals(p))) {
      return false;
    }
    if (el.getAnnotation(jakarta.validation.constraints.NotNull.class) != null 
            || el.getAnnotation(javax.annotation.Nonnull.class) != null // no replacement in jakarta
            || el.getAnnotation(jakarta.validation.constraints.NotEmpty.class) != null 
            || el.getAnnotation(jakarta.validation.constraints.NotBlank.class) != null) {
      return true;
    }
    if (el.getAnnotation(javax.annotation.Nullable.class) != null) {
      return false;
    }
    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        try {
          if (annotationElement.getAnnotation(jakarta.validation.constraints.NotNull.class) != null) {
            return true;
          }
          if (recurse && isNotNull(annotationElement, false)) {
            return true;
          }
        }
        catch (Exception e) {
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
          if (pckg instanceof PackageElement) {
            String packageQualifiedName = ((PackageElement) pckg).getQualifiedName().toString();
            boolean nameEqualsJavax = packageQualifiedName.equals("jakarta.validation.constraints");
            boolean nameEqualsJakarta = packageQualifiedName.equals("jakarta.validation.constraints");
            if (nameEqualsJakarta || nameEqualsJavax) {
              return true;
            }
          }
          if (AllowedValues.class.getName().equals(((TypeElement)annotationElement).getQualifiedName().toString())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static String describeConstraints(Element el, boolean required, boolean explicitlyNotRequired, String defaultValue, DecoratedProcessingEnvironment env) {
    jakarta.validation.constraints.Null mustBeNull = el.getAnnotation(jakarta.validation.constraints.Null.class);
    jakarta.validation.constraints.Null mustBeNull2 = el.getAnnotation(jakarta.validation.constraints.Null.class);
    if (mustBeNull != null || mustBeNull2 != null) {
      return "must be null";
    }
    
    String type = describeTypeIfPrimitive(el);

    List<String> constraints = new ArrayList<String>();
    required = required || (isNotNull(el) && defaultValue == null);
    if (required && !explicitlyNotRequired) {
      constraints.add("required" + type);
    }

    ReadOnly readOnly = el.getAnnotation(ReadOnly.class);
    if (readOnly != null) {
      constraints.add("read-only" + type);
    }

    jakarta.validation.constraints.AssertFalse mustBeFalse = el.getAnnotation(jakarta.validation.constraints.AssertFalse.class);
    jakarta.validation.constraints.AssertFalse mustBeFalse2 = el.getAnnotation(jakarta.validation.constraints.AssertFalse.class);
    if (mustBeFalse != null || mustBeFalse2 != null) {
      constraints.add("must be \"false\"");
    }

    jakarta.validation.constraints.AssertTrue mustBeTrue = el.getAnnotation(jakarta.validation.constraints.AssertTrue.class);
    jakarta.validation.constraints.AssertTrue mustBeTrue2 = el.getAnnotation(jakarta.validation.constraints.AssertTrue.class);
    if (mustBeTrue != null || mustBeTrue2 != null) {
      constraints.add("must be \"true\"");
    }

    jakarta.validation.constraints.DecimalMax decimalMax = el.getAnnotation(jakarta.validation.constraints.DecimalMax.class);
    jakarta.validation.constraints.DecimalMax decimalMax2 = el.getAnnotation(jakarta.validation.constraints.DecimalMax.class);
    if (decimalMax != null || decimalMax2 != null) {
        String value = decimalMax != null ? decimalMax.value() : decimalMax2.value();
        boolean inclusive = decimalMax != null ? decimalMax.inclusive() : decimalMax2.inclusive();
      constraints.add("max: " + value + (inclusive ? "" : " (exclusive)"));
    }

    jakarta.validation.constraints.DecimalMin decimalMin = el.getAnnotation(jakarta.validation.constraints.DecimalMin.class);
    jakarta.validation.constraints.DecimalMin decimalMin2 = el.getAnnotation(jakarta.validation.constraints.DecimalMin.class);
    if (decimalMin != null || decimalMin2 != null) {
        String value = decimalMin != null ? decimalMin.value() : decimalMin2.value();
        boolean inclusive = decimalMin != null ? decimalMin.inclusive() : decimalMin2.inclusive();
      constraints.add("min: " + value + (inclusive ? "" : " (exclusive)"));
    }

    jakarta.validation.constraints.Digits digits = el.getAnnotation(jakarta.validation.constraints.Digits.class);
    jakarta.validation.constraints.Digits digits2 = el.getAnnotation(jakarta.validation.constraints.Digits.class);
    if (digits != null || digits2 != null) {
        int integer = digits != null ? digits.integer() : digits2.integer();
        int fraction = digits != null ? digits.fraction() : digits2.fraction();
      constraints.add("max digits: " + integer + " (integer), " + fraction + " (fraction)");
    }

    jakarta.validation.constraints.Future dateInFuture = el.getAnnotation(jakarta.validation.constraints.Future.class);
    jakarta.validation.constraints.Future dateInFuture2 = el.getAnnotation(jakarta.validation.constraints.Future.class);
    if (dateInFuture != null || dateInFuture2 != null) {
      constraints.add("future date");
    }

    jakarta.validation.constraints.Max max = el.getAnnotation(jakarta.validation.constraints.Max.class);
    jakarta.validation.constraints.Max max2 = el.getAnnotation(jakarta.validation.constraints.Max.class);
    if (max != null || max2 != null) {
      constraints.add("max: " + (max != null ? max.value() : max2.value()));
    }

    jakarta.validation.constraints.Min min = el.getAnnotation(jakarta.validation.constraints.Min.class);
    jakarta.validation.constraints.Min min2 = el.getAnnotation(jakarta.validation.constraints.Min.class);
    if (min != null || min2 != null) {
      constraints.add("min: " + (min != null ? min.value() : min2.value()));
    }

    jakarta.validation.constraints.Past dateInPast = el.getAnnotation(jakarta.validation.constraints.Past.class);
    jakarta.validation.constraints.Past dateInPast2 = el.getAnnotation(jakarta.validation.constraints.Past.class);
    if (dateInPast != null || dateInPast2 != null) {
      constraints.add("past date");
    }

    jakarta.validation.constraints.Pattern mustMatchPattern = el.getAnnotation(jakarta.validation.constraints.Pattern.class);
    jakarta.validation.constraints.Pattern mustMatchPattern2 = el.getAnnotation(jakarta.validation.constraints.Pattern.class);
    if (mustMatchPattern != null || mustMatchPattern2 != null) {
      constraints.add("regex: " + (mustMatchPattern != null ? mustMatchPattern.regexp() : mustMatchPattern2.regexp()));
    }

    AllowedValues allowedValues = el.getAnnotation(AllowedValues.class);
    if (allowedValues != null) {
      DecoratedTypeMirror enumMirror = Annotations.mirrorOf(allowedValues::value, env);
      String values = ((DecoratedTypeElement) ((DecoratedDeclaredType) enumMirror).asElement()).enumValues().stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", "));
      constraints.add("values: " + values);
    }

    jakarta.validation.constraints.Size size = el.getAnnotation(jakarta.validation.constraints.Size.class);
    jakarta.validation.constraints.Size size2 = el.getAnnotation(jakarta.validation.constraints.Size.class);
    if (size != null || size2 != null) {
      constraints.add("max size: " + (size != null ? size.max() : size2.max()) + ", min size: " + (size != null ? size.min() : size2.min()));
    }

    jakarta.validation.constraints.Email email = el.getAnnotation(jakarta.validation.constraints.Email.class);
    jakarta.validation.constraints.Email email2 = el.getAnnotation(jakarta.validation.constraints.Email.class);
    if (email != null || email2 != null) {
      constraints.add("e-mail");
    }

    jakarta.validation.constraints.NotEmpty notEmpty = el.getAnnotation(jakarta.validation.constraints.NotEmpty.class);
    jakarta.validation.constraints.NotEmpty notEmpty2 = el.getAnnotation(jakarta.validation.constraints.NotEmpty.class);
    if (notEmpty != null || notEmpty2 != null) {
      constraints.add("not empty" + type);
    }

    jakarta.validation.constraints.NotBlank notBlank = el.getAnnotation(jakarta.validation.constraints.NotBlank.class);
    jakarta.validation.constraints.NotBlank notBlank2 = el.getAnnotation(jakarta.validation.constraints.NotBlank.class);
    if (notBlank != null || notBlank2 != null) {
      constraints.add("not blank" + type);
    }

    jakarta.validation.constraints.Positive positive = el.getAnnotation(jakarta.validation.constraints.Positive.class);
    jakarta.validation.constraints.Positive positive2 = el.getAnnotation(jakarta.validation.constraints.Positive.class);
    if (positive != null || positive2 != null) {
      constraints.add("positive" + type);
    }

    jakarta.validation.constraints.PositiveOrZero positiveOrZero = el.getAnnotation(jakarta.validation.constraints.PositiveOrZero.class);
    jakarta.validation.constraints.PositiveOrZero positiveOrZero2 = el.getAnnotation(jakarta.validation.constraints.PositiveOrZero.class);
    if (positiveOrZero != null || positiveOrZero2 != null) {
      constraints.add("positive or zero" + type);
    }

    jakarta.validation.constraints.Negative negative = el.getAnnotation(jakarta.validation.constraints.Negative.class);
    jakarta.validation.constraints.Negative negative2 = el.getAnnotation(jakarta.validation.constraints.Negative.class);
    if (negative != null || negative2 != null) {
      constraints.add("negative" + type);
    }

    jakarta.validation.constraints.NegativeOrZero negativeOrZero = el.getAnnotation(jakarta.validation.constraints.NegativeOrZero.class);
    jakarta.validation.constraints.NegativeOrZero negativeOrZero2 = el.getAnnotation(jakarta.validation.constraints.NegativeOrZero.class);
    if (negativeOrZero != null || negativeOrZero2 != null) {
      constraints.add("negative or zero" + type);
    }

    jakarta.validation.constraints.PastOrPresent pastOrPresent = el.getAnnotation(jakarta.validation.constraints.PastOrPresent.class);
    jakarta.validation.constraints.PastOrPresent pastOrPresent2 = el.getAnnotation(jakarta.validation.constraints.PastOrPresent.class);
    if (pastOrPresent != null || pastOrPresent2 != null) {
      constraints.add("past or present");
    }

    jakarta.validation.constraints.FutureOrPresent futureOrPresent = el.getAnnotation(jakarta.validation.constraints.FutureOrPresent.class);
    jakarta.validation.constraints.FutureOrPresent futureOrPresent2 = el.getAnnotation(jakarta.validation.constraints.FutureOrPresent.class);
    if (futureOrPresent != null || futureOrPresent2 != null) {
      constraints.add("future or present");
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

  private static String describeTypeIfPrimitive(Element el) {
    final TypeMirror type = el.asType();
    if (type instanceof PrimitiveType) {
      return " " + type.getKind().name().toLowerCase();
    }
    else {
      switch (type.toString()) {
        case "java.lang.Integer":
          return " int";
        case "java.lang.Short":
          return " short";
        case "java.lang.Float":
          return " float";
        case "java.lang.Double":
          return " double";
        case "java.lang.Long":
          return " long";
        case "java.lang.Character":
          return " char";
        case "java.lang.Byte":
          return " byte";
        case "java.lang.Boolean":
          return " boolean";
      }
    }
    return "";
  }
}
