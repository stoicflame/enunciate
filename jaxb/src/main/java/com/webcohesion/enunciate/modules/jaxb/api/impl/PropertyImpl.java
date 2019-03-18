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
package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.ReadOnly;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.Value;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PropertyImpl implements Property {

  private final Accessor accessor;
  private ApiRegistrationContext registrationContext;

  public PropertyImpl(Accessor accessor, ApiRegistrationContext registrationContext) {
    this.accessor = accessor;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getName() {
    if (this.accessor.isValue()) {
      return "(value)";
    }

    return this.accessor.getName();
  }

  public String getType() {
    return this.accessor.isAttribute() ? "attribute" : this.accessor.isValue() ? "(value)" : "element";
  }

  public PropertyMetadata getNamespaceInfo() {
    return new PropertyMetadata(getNamespacePrefix(), getNamespace(), null);
  }

  public String getNamespacePrefix() {
    String namespace = getNamespace();
    String prefix = this.accessor.getContext().getNamespacePrefixes().get(namespace);
    if (namespace == null || "".equals(namespace)) {
      prefix = "";
    }
    return prefix;
  }

  public String getNamespace() {
    return this.accessor.getNamespace();
  }

  @Override
  public boolean isRequired() {
    if (this.accessor instanceof Attribute) {
      return ((Attribute) this.accessor).isRequired();
    }
    else if (this.accessor instanceof Value) {
      return true;
    }
    else if (this.accessor instanceof Element) {
      return ((Element) this.accessor).isRequired();
    }

    return false;
  }

  @Override
  public boolean isReadOnly() {
    return accessor.getAnnotation(ReadOnly.class) != null || accessor.getJavaDoc().get("readonly") != null;
  }

  public String getMinMaxOccurs() {
    String minMaxOccurs = null;

    if (this.accessor instanceof Attribute) {
      minMaxOccurs = String.format("%s/1", ((Attribute) this.accessor).isRequired() ? "1" : "0");
    }
    else if (this.accessor instanceof Value) {
      minMaxOccurs = "0/1";
    }
    else if (this.accessor instanceof Element) {
      minMaxOccurs = String.format("%s/%s", ((Element)this.accessor).getMinOccurs(), ((Element)this.accessor).getMaxOccurs());
    }

    return minMaxOccurs;
  }

  public String getDefaultValue() {
    String defaultValue = null;
    if (this.accessor instanceof Element) {
      defaultValue = ((Element) this.accessor).getDefaultValue();
    }
    return defaultValue;
  }

  public String getConstraints() {
    boolean required = false;
    if (this.accessor instanceof Element) {
        required = ((Element) this.accessor).isRequired();
    } else if (this.accessor instanceof Attribute) {
        required = ((Attribute) this.accessor).isRequired();
    }
    return BeanValidationUtils.describeConstraints(this.accessor, required);
  }

  @Override
  public String getDescription() {
    JavaDoc doc = this.accessor.getJavaDoc(this.registrationContext.getTagHandler());

    String description = doc.toString();

    if (description.trim().isEmpty()) {
      description = null;
    }

    if (description == null) {
      JavaDoc.JavaDocTagList tags = doc.get("return");
      if (tags != null && !tags.isEmpty()) {
        description = tags.toString();
      }
    }

    return description;
  }

  public boolean isAttribute() {
    return this.accessor.isAttribute();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(accessor.getXmlType(), accessor.isCollectionType(), registrationContext);
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.accessor, this.registrationContext.getTagHandler());
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.accessor.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.accessor.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.accessor.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.accessor.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.accessor, this.accessor.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList sinceTags = getJavaDoc().get("since");
    return sinceTags == null ? null : sinceTags.toString();
  }
}
