package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl implements ResourceGroup {

  private final com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass;
  private final List<Resource> resources = new ArrayList<Resource>();
  private final String contextPath;

  public ResourceClassResourceGroupImpl(com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass, String contextPath) {
    this.resourceClass = resourceClass;
    this.contextPath = contextPath;
    FacetFilter facetFilter = resourceClass.getContext().getContext().getConfiguration().getFacetFilter();
    for (ResourceMethod resourceMethod : resourceClass.getResourceMethods(true)) {
      if (!facetFilter.accept(resourceMethod)) {
        continue;
      }

      this.resources.add(new ResourceImpl(resourceMethod, this));
    }
  }

  @Override
  public String getSlug() {
    return "resource_" + resourceClass.getSimpleName().toString();
  }

  @Override
  public String getLabel() {
    return resourceClass.getSimpleName().toString();
  }

  @Override
  public String getContextPath() {
    return this.contextPath;
  }

  @Override
  public String getDescription() {
    return resourceClass.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.resourceClass);
  }

  @Override
  public Set<String> getPaths() {
    TreeSet<String> paths = new TreeSet<String>();
    for (Resource resource : this.resources) {
      paths.add(resource.getPath());
    }
    return paths;
  }

  @Override
  public Set<String> getMethods() {
    TreeSet<String> methods = new TreeSet<String>();
    for (Resource resource : this.resources) {
      for (Method method : resource.getMethods()) {
        methods.add(method.getLabel());
      }
    }
    return methods;
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }
}
