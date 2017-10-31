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
package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.modules.jackson1.model.Member;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.util.BeanValidationUtils;
import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public abstract class DataTypeImpl implements DataType {

  private final TypeDefinition typeDefinition;
  protected final ApiRegistrationContext registrationContext;

  protected DataTypeImpl(TypeDefinition typeDefinition, ApiRegistrationContext registrationContext) {
    this.typeDefinition = typeDefinition;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getLabel() {
    String label = this.typeDefinition.getSimpleName().toString();

    JsonRootName rootName = this.typeDefinition.getAnnotation(JsonRootName.class);
    label = rootName == null ? label : rootName.value();

    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      label = tag.isEmpty() ? label : tag;
    }

    Label labelInfo = this.typeDefinition.getAnnotation(Label.class);
    label = labelInfo == null ? label : labelInfo.value();

    return label;
  }

  @Override
  public String getSlug() {
    return "json_" + this.typeDefinition.getContext().getSlug(this.typeDefinition);
  }

  @Override
  public String getDescription() {
    return this.typeDefinition.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.typeDefinition);
  }

  @Override
  public Namespace getNamespace() {
    return new SyntaxImpl(this.typeDefinition.getContext(), this.registrationContext);
  }

  @Override
  public Syntax getSyntax() {
    return new SyntaxImpl(this.typeDefinition.getContext(), this.registrationContext);
  }

  @Override
  public List<DataTypeReference> getSupertypes() {
    return null;
  }

  @Override
  public Set<DataTypeReference> getInterfaces() {
    return null;
  }

  @Override
  public List<DataTypeReference> getSubtypes() {
    return null;
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    if (tags == null) {
      tags = ((DecoratedElement) this.typeDefinition.getPackage()).getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    }
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<String> getSeeAlso() {
    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc(this.registrationContext.getTagHandler()).get("see");
    if (tags == null) {
      tags = ((DecoratedElement) this.typeDefinition.getPackage()).getJavaDoc(this.registrationContext.getTagHandler()).get("see");
    }
    return tags;
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc(this.registrationContext.getTagHandler()).get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public boolean isAbstract() {
    return this.typeDefinition.isAbstract();
  }

  @Override
  public Example getExample() {
    return null;
  }

  @Override
  public Map<String, String> getPropertyMetadata() {
    Map<String, String> propertyMetadata = new LinkedHashMap<String, String>();
    boolean showRequired = false;
    boolean showDefaultValue = false;
    for (Member member : this.typeDefinition.getMembers()) {
      if (BeanValidationUtils.hasConstraints(member, member.isRequired())) {
        showRequired = true;
      }

      if (member.getDefaultValue() != null) {
        showDefaultValue = true;
      }
    }

    if (showRequired) {
      propertyMetadata.put("constraints", "constraints");
    }

    if (showDefaultValue) {
      propertyMetadata.put("defaultValue", "default");
    }

    return propertyMetadata;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.typeDefinition.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.typeDefinition.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.typeDefinition.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.typeDefinition.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.typeDefinition, this.typeDefinition.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  @Override
  public Element getJavaElement() {
    return this.typeDefinition;
  }
  
  @Override
  public String getXmlName() {
	  return null;
  }
}
