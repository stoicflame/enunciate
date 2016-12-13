package com.webcohesion.enunciate.modules.lombok;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * @author Tomasz Kalkosiński
 */
public class LombokMethodGenerator {

  private DecoratedTypeElement decoratedTypeElement;
  private DecoratedProcessingEnvironment env;

  public LombokMethodGenerator(DecoratedTypeElement decoratedTypeElement, DecoratedProcessingEnvironment env) {
    this.decoratedTypeElement = decoratedTypeElement;
    this.env = env;
  }

  public void generateLombokGettersAndSetters() {
    List<? extends VariableElement> fields = decoratedTypeElement.getFields();
    for (VariableElement field : fields) {
      if (shouldGenerateGetter(field)) {
        decoratedTypeElement.getMethods().add(new DecoratedExecutableElement(new LombokGeneratedGetter(field, env), env));
      }
      if (shouldGenerateSetter(field)) {
        decoratedTypeElement.getMethods().add(new DecoratedExecutableElement(new LombokGeneratedSetter(field, env), env));
      }
    }
  }

  private boolean shouldGenerateGetter(Element field) {
    String fieldSimpleName = field.getSimpleName().toString();
    for (ExecutableElement method : decoratedTypeElement.getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isGetter()) {
        return false;
      }
    }

    if (field.getAnnotation(Getter.class) != null) {
      return true;
    }

    if (decoratedTypeElement.getAnnotation(Getter.class) != null) {
      return true;
    }

    if (decoratedTypeElement.getAnnotation(Data.class) != null) {
      return true;
    }

    return false;
  }

  private boolean shouldGenerateSetter(Element field) {
    String fieldSimpleName = field.getSimpleName().toString();
    for (ExecutableElement method : decoratedTypeElement.getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isSetter()) {
        return false;
      }
    }

    if (field.getAnnotation(Setter.class) != null) {
      return true;
    }

    if (decoratedTypeElement.getAnnotation(Setter.class) != null) {
      return true;
    }

    if (decoratedTypeElement.getAnnotation(Data.class) != null) {
      return true;
    }

    return false;
  }
}