package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class PathBasedResourceGroupImpl implements ResourceGroup {

  private final String contextPath;
  private final String path;
  private final List<Resource> resources;

  public PathBasedResourceGroupImpl(String contextPath, String path, List<Resource> resources) {
    this.contextPath = contextPath;
    this.path = path;
    this.resources = resources;
  }

  @Override
  public String getSlug() {
    return "resource_" + scrubPathForSlug(path);
  }

  @Override
  public String getRelativeContextPath() {
    return this.contextPath;
  }

  @Override
  public String getLabel() {
    return this.path;
  }

  @Override
  public String getSortKey() {
    return this.path;
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
    Set<String> methods = new TreeSet<String>();
    for (Resource resource : this.resources) {
      for (Method method : resource.getMethods()) {
        methods.add(method.getHttpMethod());
      }
    }
    return Arrays.asList((PathSummary) new PathSummaryImpl(this.path, methods));
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  private static String scrubPathForSlug(String facetValue) {
    return facetValue.replace('/', '_').replace(':', '_').replace('{', '_').replace('}', '_');
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }
}
