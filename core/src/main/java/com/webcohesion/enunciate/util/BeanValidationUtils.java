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
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.AllowedValues;
import com.webcohesion.enunciate.metadata.ReadOnly;
import jakarta.validation.constraints.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.function.Function;
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
  
  private enum NoValues { }

  public static boolean isNotNull(Element el, ProcessingEnvironment penv) {
    return isNotNull(el, penv, true);
  }

  private static boolean isNotNull(Element el, ProcessingEnvironment penv, boolean recurse) {
    final TypeMirror asType = el.asType();
    if (asType instanceof PrimitiveType) {
      return true;
    }

    if (asType instanceof DeclaredType && OPTIONALS.stream().anyMatch(p -> ((QualifiedNameable) ((DeclaredType) asType).asElement()).getQualifiedName().contentEquals(p))) {
      return false;
    }

    Map<String, AnnotationMirror> validationAnnotations = gatherValidationAnnotations(el, penv);

    return isNotNull(el, validationAnnotations.keySet(), penv, recurse);
  }

  private static boolean isNotNull(Element el, Set<String> validationAnnotations, ProcessingEnvironment penv, boolean recurse) {
    if (CollectionUtils.containsAny(validationAnnotations, NotNull.class.getName(), javax.annotation.Nonnull.class.getName(), NotEmpty.class.getName(), NotBlank.class.getName())) {
      return true;
    }

    if (validationAnnotations.contains(javax.annotation.Nullable.class.getName())) {
      return false;
    }

    List<? extends AnnotationMirror> annotations = el.getAnnotationMirrors();
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      if (annotationType != null) {
        Element annotationElement = annotationType.asElement();
        try {
          if (validationAnnotations.contains(NotNull.class.getName())) {
            return true;
          }
          if (recurse && isNotNull(annotationElement, penv, false)) {
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

  public static boolean hasConstraints(Element el, boolean required, ProcessingEnvironment penv) {
    if (required) {
      return true;
    }

    Map<String, AnnotationMirror> validations = gatherValidationAnnotations(el, penv); 
    return !validations.isEmpty();
  }

  public static String describeConstraints(Element el, boolean required, boolean explicitlyNotRequired, String defaultValue, DecoratedProcessingEnvironment env) {
    Map<String, AnnotationMirror> validations = gatherValidationAnnotations(el, env);
   
    if (validations.containsKey(Null.class.getName())) {
      return "must be null";
    }
    
    String type = describeTypeIfPrimitive(el);

    List<String> constraints = new ArrayList<>();
    required = required || (isNotNull(el, validations.keySet(), env, true) && defaultValue == null);
    if (required && !explicitlyNotRequired) {
      constraints.add("required" + type);
    }

    if (validations.containsKey(ReadOnly.class.getName())) {
      constraints.add("read-only" + type);
    }

    if (validations.containsKey(AssertFalse.class.getName())) {
      constraints.add("must be \"false\"");
    }

    if (validations.containsKey(AssertTrue.class.getName())) {
      constraints.add("must be \"true\"");
    }

    if (validations.containsKey(DecimalMax.class.getName())) {
      AnnotationMirror mirror = validations.get(DecimalMax.class.getName());
      constraints.add("max: " + get(mirror, "value", "???") + (get(mirror, "inclusive", true) ? "" : " (exclusive)"));
    }

    if (validations.containsKey(DecimalMin.class.getName())) {
      AnnotationMirror mirror = validations.get(DecimalMin.class.getName());
      constraints.add("min: " + get(mirror, "value", "???") + (get(mirror, "inclusive", true) ? "" : " (exclusive)"));
    }

    if (validations.containsKey(Digits.class.getName())) {
      AnnotationMirror mirror = validations.get(Digits.class.getName());
      constraints.add("max digits: " + get(mirror, "integer", -1) + " (integer), " + get(mirror, "fraction", -1) + " (fraction)");
    }

    if (validations.containsKey(Future.class.getName())) {
      constraints.add("future date");
    }

    if (validations.containsKey(Max.class.getName())) {
      AnnotationMirror mirror = validations.get(Max.class.getName());
      constraints.add("max: " + get(mirror, "value", Long.MAX_VALUE));
    }

    if (validations.containsKey(Min.class.getName())) {
      AnnotationMirror mirror = validations.get(Min.class.getName());
      constraints.add("min: " + get(mirror, "value", 0L));
    }

    if (validations.containsKey(Past.class.getName())) {
      constraints.add("past date");
    }

    if (validations.containsKey(Pattern.class.getName())) {
      AnnotationMirror mirror = validations.get(Pattern.class.getName());
      constraints.add("regex: " + get(mirror, "regexp", "???"));
    }

    if (validations.containsKey(AllowedValues.class.getName())) {
      AnnotationMirror mirror = validations.get(AllowedValues.class.getName());
      DecoratedTypeMirror enumMirror = Annotations.mirrorOf(() -> get(mirror, "value", NoValues.class), env);
      String values = ((DecoratedTypeElement) ((DecoratedDeclaredType) enumMirror).asElement()).enumValues().stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", "));
      constraints.add("values: " + values);
    }

    if (validations.containsKey(Size.class.getName())) {
      AnnotationMirror mirror = validations.get(Size.class.getName());
      constraints.add("max size: " + get(mirror, "max", Integer.MAX_VALUE) + ", min size: " + get(mirror, "min", 0));
    }

    if (validations.containsKey(Email.class.getName())) {
      constraints.add("e-mail");
    }

    if (validations.containsKey(NotEmpty.class.getName())) {
      constraints.add("not empty" + type);
    }

    if (validations.containsKey(NotBlank.class.getName())) {
      constraints.add("not blank" + type);
    }

    if (validations.containsKey(Positive.class.getName())) {
      constraints.add("positive" + type);
    }

    if (validations.containsKey(PositiveOrZero.class.getName())) {
      constraints.add("positive or zero" + type);
    }

    if (validations.containsKey(Negative.class.getName())) {
      constraints.add("negative" + type);
    }

    if (validations.containsKey(NegativeOrZero.class.getName())) {
      constraints.add("negative or zero" + type);
    }

    if (validations.containsKey(PastOrPresent.class.getName())) {
      constraints.add("past or present");
    }

    if (validations.containsKey(FutureOrPresent.class.getName())) {
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
  
  private static <T> T get(AnnotationMirror mirror, String element, T defaultValue) {
    return (T) mirror.getElementValues().entrySet().stream().filter(e -> e.getKey().getSimpleName().toString().equals(element))
       .findFirst()
       .map(Map.Entry::getValue)
       .map(AnnotationValue::getValue)
       .orElse(defaultValue);
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

  private static Map<String, AnnotationMirror> gatherValidationAnnotations(Element el, ProcessingEnvironment penv) {
    Map<String, AnnotationMirror> annotations = new HashMap<>();
    gatherValidationAnnotations(annotations, el);
    visitOverriddenMethods(candidate -> {
      gatherValidationAnnotations(annotations, candidate);
      return null;
    }, el, el.getEnclosingElement(), penv);
    return annotations;
  }

  private static <R> R visitOverriddenMethods(Function<ExecutableElement, R> visitor, Element el, Element enclosing, ProcessingEnvironment penv) {
    if (enclosing == null || (enclosing instanceof TypeElement && Object.class.getName().equals(((TypeElement) enclosing).getQualifiedName().toString()))) {
      return null;
    }

    while (el instanceof DecoratedElement) {
      el = ((DecoratedElement)el).getDelegate();
    }

    if (!(el instanceof ExecutableElement)) {
      return null;
    }

    Elements elements = penv.getElementUtils();
    if (enclosing instanceof TypeElement) {
      List<? extends Element> candidates = enclosing.getEnclosedElements();
      for (Element candidate : candidates) {
        if (candidate instanceof ExecutableElement && elements.overrides((ExecutableElement) el, (ExecutableElement) candidate, (TypeElement) enclosing)) {
          R result = visitor.apply((ExecutableElement) candidate);
          if (result != null) {
            return result;
          }
        }
      }

      List<? extends TypeMirror> ifaces = ((TypeElement) enclosing).getInterfaces();
      if (ifaces != null) {
        for (TypeMirror iface : ifaces) {
          TypeElement iel = (TypeElement) ((DeclaredType) iface).asElement();
          R result = visitOverriddenMethods(visitor, el, iel, penv);
          if (result != null) {
            return result;
          }
        }
      }
    }

    TypeMirror superclass = ((TypeElement) enclosing).getSuperclass();
    if (superclass.getKind() == TypeKind.DECLARED) {
      return visitOverriddenMethods(visitor, el, ((DeclaredType) superclass).asElement(), penv);
    } else {
      return null;
    }
  }

  private static void gatherValidationAnnotations(Map<String, AnnotationMirror> bucket, Element el) {
    CollectionUtils.emptyIfNull(el.getAnnotationMirrors()).stream()
       .filter(annotation -> annotation.getAnnotationType() instanceof DeclaredType && isValidationAnnotation((TypeElement) annotation.getAnnotationType().asElement()))
       .forEach(annotation -> bucket.put(((TypeElement)annotation.getAnnotationType().asElement()).getQualifiedName().toString(), annotation));
  }

  private static boolean isValidationAnnotation(TypeElement el) {
    if (StringUtils.startsWith(el.getQualifiedName().toString(), "jakarta.validation.constraints.")) {
      return true;
    }

    if (StringUtils.equals(el.getQualifiedName().toString(), ReadOnly.class.getName())) {
      return true;
    }
    
    if (StringUtils.equals(el.getQualifiedName().toString(), ParametersAreNonnullByDefault.class.getName())) {
      return true;
    }
    
    if (StringUtils.equals(el.getQualifiedName().toString(), javax.annotation.Nonnull.class.getName())) {
      return true;
    }
    
    if (StringUtils.equals(el.getQualifiedName().toString(), javax.annotation.Nullable.class.getName())) {
      return true;
    }
    
    return false; 
  }
}
