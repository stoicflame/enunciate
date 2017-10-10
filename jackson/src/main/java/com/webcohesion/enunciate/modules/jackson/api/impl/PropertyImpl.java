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
package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.ReadOnly;
import com.webcohesion.enunciate.modules.jackson.model.Member;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonArrayType;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PropertyImpl implements Property {

  private final Member member;
  private ApiRegistrationContext registrationContext;
  private final boolean plural;

  public PropertyImpl(Member member, ApiRegistrationContext registrationContext) {
    this(member, registrationContext, false);
  }

  public PropertyImpl(Member member, ApiRegistrationContext registrationContext, boolean plural) {
    this.member = member;
    this.registrationContext = registrationContext;
    this.plural = plural;
  }

  @Override
  public String getName() {
    return this.member.getName();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(this.plural ? new JsonArrayType(this.member.getJsonType()) : this.member.getJsonType(), registrationContext);
  }

  @Override
  public String getDescription() {
    return this.member.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.member);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.member.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.member.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.member.getFacets();
  }

  @Override
  public boolean isRequired() {
    return member.isRequired();
  }

  @Override
  public boolean isReadOnly() {
    return member.getAnnotation(ReadOnly.class) != null || member.getJavaDoc().get("readonly") != null;
  }

  public String getConstraints() {
    return BeanValidationUtils.describeConstraints(member, isRequired());
  }

  public String getDefaultValue() {
    return member.getDefaultValue();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return member.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.member, this.member.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList sinceTags = getJavaDoc().get("since");
    return sinceTags == null ? null : sinceTags.toString();
  }
}
