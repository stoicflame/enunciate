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
package com.webcohesion.enunciate.modules.spring_web.api.impl;

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
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.SpringController;
import com.webcohesion.enunciate.util.PathSummaryComparator;
import com.webcohesion.enunciate.util.ResourceComparator;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl implements ResourceGroup {

  private final SpringController controllerClass;
  private final List<Resource> resources = new ArrayList<Resource>();
  private final String contextPath;
  private final String slug;
  private final ApiRegistrationContext registrationContext;

  public ResourceClassResourceGroupImpl(SpringController controllerClass, String slug, String contextPath, ApiRegistrationContext registrationContext) {
    this.controllerClass = controllerClass;
    this.slug = slug;
    this.contextPath = contextPath;
    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (RequestMapping requestMapping : controllerClass.getRequestMappings()) {
      if (!facetFilter.accept(requestMapping)) {
        continue;
      }

      this.resources.add(new ResourceImpl(requestMapping, this, registrationContext));
    }

    Collections.sort(this.resources, new ResourceComparator(controllerClass.getContext().getPathSortStrategy()));
    this.registrationContext = registrationContext;
  }

  @Override
  public String getSlug() {
    return "resource_" + this.slug;
  }

  @Override
  public String getLabel() {
    String label = controllerClass.getSimpleName().toString();

    ResourceLabel resourceLabel = controllerClass.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null && !"##default".equals(resourceLabel.value())) {
      label = resourceLabel.value();
    }

    Label generic = controllerClass.getAnnotation(Label.class);
    if (generic != null) {
      label = generic.value();
    }

    JavaDoc.JavaDocTagList tags = this.controllerClass.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      label = tag.isEmpty() ? label : tag;
    }

    return label;
  }

  @Override
  public String getSortKey() {
    String sortKey = getLabel();
    ResourceLabel resourceLabel = controllerClass.getAnnotation(ResourceLabel.class);
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
    return controllerClass.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.controllerClass, this.registrationContext.getTagHandler());
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
    Collections.sort(pathSummaries, new PathSummaryComparator(controllerClass.getContext().getPathSortStrategy()));
    return pathSummaries;
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.controllerClass.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.controllerClass.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.controllerClass.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.controllerClass.getJavaDoc(this.registrationContext.getTagHandler());
  }


  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.controllerClass, this.controllerClass.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
