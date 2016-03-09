package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.util.PathSortStrategy;
import com.webcohesion.enunciate.util.PathSummaryComparator;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class AnnotationBasedResourceGroupImpl implements ResourceGroup {

  private final String contextPath;
  private final String label;
  private final List<Resource> resources;
  private final PathSortStrategy sortStrategy;

  public AnnotationBasedResourceGroupImpl(String contextPath, String label, List<Resource> resources, PathSortStrategy sortStrategy) {
    this.contextPath = contextPath;
    this.label = label;
    this.resources = resources;
    this.sortStrategy = sortStrategy;
  }

  @Override
  public String getSlug() {
    return "resource_" + scrubLabelForSlug(label);
  }

  @Override
  public String getRelativeContextPath() {
    return this.contextPath;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public String getSortKey() {
    return this.label;
  }

  @Override
  public String getDescription() {
    //we'll return a description if all descriptions of all methods are the same.
    String description = null;
    for (Resource resource : this.resources) {
      for (Method method : resource.getMethods()) {
        if (description != null && method.getDescription() != null && !description.equals(method.getDescription())){
          return null;
        }

        description = method.getDescription();
      }
    }

    return description;
  }

  @Override
  public String getDeprecated() {
    String deprecated = null;
    for (Resource resource : this.resources) {
      deprecated = resource.getDeprecated();
      if (deprecated == null) {
        //if _any_ resources are not deprecated, this resource group isn't deprecated either.
        return null;
      }
    }
    return deprecated;
  }

  @Override
  public List<PathSummary> getPaths() {
    HashMap<String, PathSummary> paths = new HashMap<String, PathSummary>();
    for (Resource resource : this.resources) {
      PathSummary pathSummary = paths.get(resource.getPath());
      if (pathSummary == null) {
        pathSummary = new PathSummaryImpl(resource.getPath(), new TreeSet<String>());
        paths.put(resource.getPath(), pathSummary);
      }

      for (Method method : resource.getMethods()) {
        pathSummary.getMethods().add(method.getHttpMethod());
      }
    }
    ArrayList<PathSummary> pathSummaries = new ArrayList<PathSummary>(paths.values());
    Collections.sort(pathSummaries, new PathSummaryComparator(sortStrategy));
    return pathSummaries;
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  private static String scrubLabelForSlug(String facetValue) {
    return facetValue.replace('/', '_').replace(':', '_').replace('{', '_').replace('}', '_').replace(' ', '_');
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }
}
