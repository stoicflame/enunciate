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
public class FacetBasedResourceGroupImpl implements ResourceGroup {

  protected final String facetValue;
  protected final List<Resource> resources;

  public FacetBasedResourceGroupImpl(String facetValue, List<Resource> resources) {
    this.facetValue = facetValue;
    this.resources = resources;
  }

  @Override
  public String getSlug() {
    return "resource_" + scrubForSlug(facetValue);
  }

  @Override
  public String getLabel() {
    return this.facetValue;
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

  private static String scrubForSlug(String facetValue) {
    return facetValue.replace('/', '_').replace(':', '_').replace('{', '_').replace('}', '_');
  }

}
