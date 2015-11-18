package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.SpringController;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl implements ResourceGroup {

  private final SpringController controllerClass;
  private final List<Resource> resources = new ArrayList<Resource>();
  private final String contextPath;

  public ResourceClassResourceGroupImpl(SpringController controllerClass, String contextPath) {
    this.controllerClass = controllerClass;
    this.contextPath = contextPath;
    FacetFilter facetFilter = controllerClass.getContext().getContext().getConfiguration().getFacetFilter();
    for (RequestMapping requestMapping : controllerClass.getRequestMappings()) {
      if (!facetFilter.accept(requestMapping)) {
        continue;
      }

      this.resources.add(new ResourceImpl(requestMapping, this));
    }
  }

  @Override
  public String getSlug() {
    return "resource_" + controllerClass.getSimpleName().toString();
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
  public String getContextPath() {
    return this.contextPath;
  }

  @Override
  public String getDescription() {
    return controllerClass.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.controllerClass);
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
        summary = new PathSummaryImpl(resource.getPath(), methods);
        summaries.put(resource.getPath(), summary);
      }
      else {
        summary.getMethods().addAll(methods);
      }
    }
    return new ArrayList<PathSummary>(summaries.values());
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.controllerClass.getAnnotations();
  }
}
