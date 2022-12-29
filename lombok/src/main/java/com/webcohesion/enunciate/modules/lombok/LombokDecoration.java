package com.webcohesion.enunciate.modules.lombok;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor9;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author Ryan Heaton
 */
public class LombokDecoration extends SimpleElementVisitor9<Void, DecoratedProcessingEnvironment> implements ElementDecoration {

  private final Map<String, List<DecoratedExecutableElement>> CACHE = new TreeMap<>();

  @Override
  public void applyTo(DecoratedElement e, DecoratedProcessingEnvironment env) {
    e.accept(this, env);
  }

  @Override
  public Void visitType(TypeElement e, DecoratedProcessingEnvironment env) {
    DecoratedTypeElement typeElement = (DecoratedTypeElement) e;
    List<DecoratedExecutableElement> methods = getLombokMethodDecorations(typeElement, env);
    typeElement.getMethods().addAll(methods);
    return null;
  }

  @Override
  public Void visitUnknown(Element e, DecoratedProcessingEnvironment env) {
    //no-op
    return null;
  }

  private List<DecoratedExecutableElement> getLombokMethodDecorations(DecoratedTypeElement element, DecoratedProcessingEnvironment env) {
    return CACHE.computeIfAbsent(element.getQualifiedName().toString(), fqn -> {
      List<DecoratedExecutableElement> methods = new ArrayList<>();
      element.getFields().forEach(field -> {
        if (shouldGenerateGetter(element, field)) {
          methods.add(new DecoratedExecutableElement(new LombokGeneratedGetter(field, env), env));
        }
        if (shouldGenerateSetter(element, field)) {
          methods.add(new DecoratedExecutableElement(new LombokGeneratedSetter(field, env), env));
        }
      });
      return methods;
    });
  }

  private boolean shouldGenerateGetter(DecoratedTypeElement element, Element field) {
    String fieldSimpleName = field.getSimpleName().toString();
    for (ExecutableElement method : element.getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isGetter()) {
        return false;
      }
    }

    return field.getAnnotation(Getter.class) != null
            || element.getAnnotation(Getter.class) != null
            || element.getAnnotation(Data.class) != null
            || element.getAnnotation(Value.class) != null;
  }

  private boolean shouldGenerateSetter(DecoratedTypeElement element, Element field) {
    String fieldSimpleName = field.getSimpleName().toString();
    for (ExecutableElement method : element.getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isSetter()) {
        return false;
      }
    }

    return field.getAnnotation(Setter.class) != null
            || element.getAnnotation(Setter.class) != null
            || element.getAnnotation(Data.class) != null
            || element.getAnnotation(Value.class) != null;
  }

}
