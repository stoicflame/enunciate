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
