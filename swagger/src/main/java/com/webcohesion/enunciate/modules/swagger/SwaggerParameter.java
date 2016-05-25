package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class SwaggerParameter implements Parameter {

  private final Parameter delegate;
  private final String type;

  public SwaggerParameter(Parameter delegate, String type) {
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getDescription() {
    return delegate.getDescription();
  }

  @Override
  public String getDefaultValue() {
    return delegate.getDefaultValue();
  }

  @Override
  public String getTypeLabel() {
    return this.type;
  }

  @Override
  public String getTypeName() {
    return delegate.getTypeName();
  }

  @Override
  public String getConstraints() {
    return delegate.getConstraints();
  }

  @Override
  public Set<String> getConstraintValues() {
    return delegate.getConstraintValues();
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.delegate.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.delegate.getJavaDoc();
  }

  @Override
  public boolean isMultivalued() {
    return this.delegate.isMultivalued();
  }

  @Override
  public Set<String> getStyles() {
    return delegate.getStyles();
  }
}
