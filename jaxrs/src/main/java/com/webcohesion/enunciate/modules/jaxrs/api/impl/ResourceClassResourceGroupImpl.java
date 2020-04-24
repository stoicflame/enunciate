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

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.PathSummaryComparator;
import com.webcohesion.enunciate.util.ResourceComparator;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl implements ResourceGroup {

  private final com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass;
  private final List<Resource> resources = new ArrayList<Resource>();
  private final String contextPath;
  private final String slug;
  private final ApiRegistrationContext registrationContext;

  public ResourceClassResourceGroupImpl(com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass, String slug, String contextPath, ApiRegistrationContext registrationContext) {
    this.resourceClass = resourceClass;
    this.contextPath = contextPath;
    this.slug = slug;
    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (ResourceMethod resourceMethod : resourceClass.getResourceMethods(true)) {
      if (!facetFilter.accept(resourceMethod)) {
        continue;
      }

      this.resources.add(new ResourceImpl(resourceMethod, this, registrationContext));
    }

    Collections.sort(this.resources, new ResourceComparator(resourceClass.getContext().getPathSortStrategy()));
    this.registrationContext = registrationContext;
  }

  @Override
  public String getSlug() {
    return "resource_" + this.slug;
  }

  @Override
  public String getLabel() {
    String label = this.resourceClass.getSimpleName().toString();
    ResourceLabel resourceLabel = resourceClass.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null && !"##default".equals(resourceLabel.value())) {
      label = resourceLabel.value();
    }

    String specifiedLabel = AnnotationUtils.getSpecifiedLabel(this.resourceClass);
    if (specifiedLabel != null) {
      label = specifiedLabel;
    }

    return label;
  }

  @Override
  public String getSortKey() {
    String sortKey = getLabel();
    ResourceLabel resourceLabel = resourceClass.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null && !"##default".equals(resourceLabel.sortKey())) {
      sortKey = resourceLabel.sortKey();
    }
    return sortKey;
  }

  @Override
  public String getRelativeContextPath() {
    return this.contextPath;
  }

  @Override
  public String getDescription() {
    return resourceClass.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.resourceClass, this.registrationContext.getTagHandler());
  }

  @Override
  public List<PathSummary> getPaths() {
    HashMap<String, PathSummary> summaries = new HashMap<String, PathSummary>();
    for (Resource resource : this.resources) {
      Set<String> methods = new TreeSet<String>();
      for (Method method : resource.getMethods()) {
        methods.add(method.getHttpMethod());
      }

      PathSummary summary = summaries.get(resource.getPath());
      if (summary == null) {
        summary = new PathSummaryImpl(resource.getPath(), methods, resource.getStyles());
        summaries.put(resource.getPath(), summary);
      }
      else {
        summary.getMethods().addAll(methods);
      }
    }
    ArrayList<PathSummary> pathSummaries = new ArrayList<PathSummary>(summaries.values());
    Collections.sort(pathSummaries, new PathSummaryComparator(resourceClass.getContext().getPathSortStrategy()));
    return pathSummaries;
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.resourceClass.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.resourceClass.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.resourceClass.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.resourceClass.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.resourceClass, this.resourceClass.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
