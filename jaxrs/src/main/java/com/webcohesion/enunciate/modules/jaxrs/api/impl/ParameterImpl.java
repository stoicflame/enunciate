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
package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceParameterConstraints;
import com.webcohesion.enunciate.util.BeanValidationUtils;

/**
 * @author Ryan Heaton
 */
public class ParameterImpl implements Parameter {

  private final ResourceParameter param;
  private final ApiRegistrationContext registrationContext;

  public ParameterImpl(ResourceParameter param, ApiRegistrationContext registrationContext) {
    this.param = param;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getName() {
    return this.param.getParameterName();
  }

  @Override
  public String getDescription() {
    if (this.param.isHeaderParam()) {
      return this.param.getDocComment();
    }
    return this.param.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getTypeLabel() {
    return this.param.getTypeName();
  }

  @Override
  public String getTypeName() {
    return this.param.getDataType().name;
  }

  @Override
  public BaseTypeFormat getTypeFormat() {
    return this.param.getDataType().format;
  }

  @Override
  public String getDefaultValue() {
    return this.param.getDefaultValue();
  }

  @Override
  public String getConstraints() {
    String validationConstraints = BeanValidationUtils.describeConstraints(this.param, false);
    if (validationConstraints != null && !validationConstraints.isEmpty()) {
      return validationConstraints;
    }

    ResourceParameterConstraints constraints = this.param.getConstraints();
    if (constraints != null && constraints.getType() != null) {
      switch (constraints.getType()) {
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
        default:
          //fall through...
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
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.param.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.param.getAnnotations();
  }

  @Override
  public boolean isMultivalued() {
    return this.param.isMultivalued();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.param.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.param, this.param.getContext().getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
