package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class PathBasedResourceGroupImpl implements ResourceGroup {

  protected final String path;
  protected final List<Resource> resources;

  public PathBasedResourceGroupImpl(String path, List<Resource> resources) {
    this.path = path;
    this.resources = resources;
  }

  @Override
  public String getSlug() {
    return "resource_" + scrubPathForSlug(path);
  }

  @Override
  public String getPath() {
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
  public List<? extends Resource> getResources() {
    return this.resources;
  }

  private static String scrubPathForSlug(String facetValue) {
    return facetValue.replace('/', '_').replace(':', '_').replace('{', '_').replace('}', '_');
  }

}
