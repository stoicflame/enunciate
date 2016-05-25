package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.spring_web.model.RequestParameter;
import com.webcohesion.enunciate.modules.spring_web.model.ResourceParameterConstraints;

import javax.lang.model.element.AnnotationMirror;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ParameterImpl implements Parameter {

  private final RequestParameter param;

  public ParameterImpl(RequestParameter param) {
    this.param = param;
  }

  @Override
  public String getName() {
    return this.param.getParameterName();
  }

  @Override
  public String getDescription() {
    return this.param.getJavaDoc().toString();
  }

  @Override
  public String getTypeLabel() {
    return this.param.getTypeName();
  }

  @Override
  public String getTypeName() {
    return this.param.getDataType().name().toLowerCase();
  }

  @Override
  public String getDefaultValue() {
    return this.param.getDefaultValue();
  }

  @Override
  public String getConstraints() {
    ResourceParameterConstraints constraints = this.param.getConstraints();
    if (constraints != null && constraints.getType() != null) {
      switch (constraints.getType()) {
        case UNBOUND_STRING:
          return null;
        case ENUMERATION:
          StringBuilder builder = new StringBuilder();
          Iterator<String> it = ((ResourceParameterConstraints.Enumeration) constraints).getValues().iterator();
          while (it.hasNext()) {
            String next = it.next();
            builder.append('"').append(next).append('"');
            if (it.hasNext()) {
              builder.append(" or ");
            }
          }
          return builder.toString();
        case PRIMITIVE:
          return ((ResourceParameterConstraints.Primitive) constraints).getKind().name().toLowerCase();
        case REGEX:
          return "regex: " + ((ResourceParameterConstraints.Regex) constraints).getRegex();
      }
    }
    return null;
  }

  @Override
  public Set<String> getConstraintValues() {
    ResourceParameterConstraints constraints = this.param.getConstraints();
    if (constraints != null && constraints.getType() != null) {
      switch (constraints.getType()) {
        case UNBOUND_STRING:
          return null;
        case ENUMERATION:
          return ((ResourceParameterConstraints.Enumeration) constraints).getValues();
        case PRIMITIVE:
          return null;
        case REGEX:
          return null;
      }
    }
    return null;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.param.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.param.getJavaDoc();
  }

  @Override
  public boolean isMultivalued() {
    return this.param.isMultivalued();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.param, Collections.<String, String>emptyMap());
  }
}
