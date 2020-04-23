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
package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResponseHeaderParameterImpl implements Parameter {

  private final String header;
  private final String description;
  private final Set<String> styles;

  public ResponseHeaderParameterImpl(String header, String description, Set<String> styles) {
    this.header = header;
    this.description = description;
    this.styles = styles;
  }

  @Override
  public String getName() {
    return this.header;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getTypeLabel() {
    return "header";
  }

  @Override
  public String getTypeName() {
    return "string"; //all headers are strings.
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public String getConstraints() {
    return null;
  }

  @Override
  public Set<String> getConstraintValues() {
    return null;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return JavaDoc.EMPTY;
  }

  @Override
  public boolean isMultivalued() {
    return false;
  }

  @Override
  public Set<String> getStyles() {
    return styles;
  }

  @Override
  public String getTypeFormat() {
    return null;
  }
}
