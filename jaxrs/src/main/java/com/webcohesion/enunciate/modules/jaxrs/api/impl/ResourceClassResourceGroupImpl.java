package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl extends FacetBasedResourceGroupImpl {

  private final com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass;

  public ResourceClassResourceGroupImpl(com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass) {
    super(resourceClass.getSimpleName().toString(), new ArrayList<Resource>());
    this.resourceClass = resourceClass;
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
  public String getDescription() {
    return resourceClass.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return DecoratedElements.findDeprecationMessage(this.resourceClass);
  }

}
