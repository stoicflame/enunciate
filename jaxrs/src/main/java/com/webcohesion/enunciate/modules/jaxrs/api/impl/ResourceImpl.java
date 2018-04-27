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

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResourceImpl implements Resource {

  final ResourceMethod resourceMethod;
  private final ResourceGroup group;
  private final List<Method> methods;
  private final ApiRegistrationContext registrationContext;

  public ResourceImpl(ResourceMethod resourceMethod, ResourceGroup group, ApiRegistrationContext registrationContext) {
    this.resourceMethod = resourceMethod;
    this.group = group;
    Set<String> httpMethods = this.resourceMethod.getHttpMethods();
    this.methods = new ArrayList<Method>(httpMethods.size());
    for (String httpMethod : httpMethods) {
      this.methods.add(new MethodImpl(httpMethod, this.resourceMethod, this.group, registrationContext));
    }
    this.registrationContext = registrationContext;
  }

  @Override
  public String getPath() {
    return resourceMethod.getFullpath();
  }

  @Override
  public String getRelativePath() {
    String relativePath = getPath();
    while (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    return relativePath;
  }

  @Override
  public String getSlug() {
    return group.getSlug() + "_" + this.resourceMethod.getSimpleName();
  }

  @Override
  public String getDeprecated() {
    String message = ElementUtils.findDeprecationMessage(this.resourceMethod, this.registrationContext.getTagHandler());
    if (message == null) {
      message = ElementUtils.findDeprecationMessage(this.resourceMethod.getParent(), this.registrationContext.getTagHandler());
    }
    return message;
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.resourceMethod.getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    if (tags == null) {
      tags = this.resourceMethod.getParent().getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    }
    if (tags == null) {
      tags = ((DecoratedElement) this.resourceMethod.getParent().getPackage()).getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    }
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<String> getSeeAlso() {
    JavaDoc.JavaDocTagList tags = this.resourceMethod.getJavaDoc(this.registrationContext.getTagHandler()).get("see");
    if (tags == null) {
      tags = this.resourceMethod.getParent().getJavaDoc(this.registrationContext.getTagHandler()).get("see");
    }
    if (tags == null) {
      tags = ((DecoratedElement) this.resourceMethod.getParent().getPackage()).getJavaDoc(this.registrationContext.getTagHandler()).get("see");
    }
    return tags;
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.resourceMethod.getJavaDoc(this.registrationContext.getTagHandler()).get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<? extends Method> getMethods() {
    return methods;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.resourceMethod.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.resourceMethod.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.resourceMethod.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.resourceMethod.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.resourceMethod, this.resourceMethod.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  @Override
  public ResourceGroup getGroup() {
    return this.group;
  }

  @Override
  public Element getJavaElement() {
    return this.resourceMethod;
  }
}
