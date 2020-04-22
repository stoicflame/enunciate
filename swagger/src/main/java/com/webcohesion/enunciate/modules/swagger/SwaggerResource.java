/*
 * © 2020 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class SwaggerResource {

  private final ResourceGroup resourceGroup;
  private final Set<Method> methods;

  public SwaggerResource(ResourceGroup resourceGroup) {
    this.resourceGroup = resourceGroup;
    this.methods = new TreeSet<>(Comparator.comparing(Method::getHttpMethod));
  }

  public ResourceGroup getResourceGroup() {
    return resourceGroup;
  }

  public Set<Method> getMethods() {
    return methods;
  }

  public String getLabel() {
    return resourceGroup.getLabel();
  }

  public String getDeprecated() {
    return resourceGroup.getDeprecated();
  }
}
