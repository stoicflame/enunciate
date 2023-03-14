/*
 * © 2020 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class SwaggerResource {

  private final ResourceGroup resourceGroup;
  private final Set<SwaggerMethod> methods = new TreeSet<>(Comparator.comparing(SwaggerMethod::getHttpMethod));

  public SwaggerResource(ResourceGroup resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public ResourceGroup getResourceGroup() {
    return resourceGroup;
  }

  public Set<Method> getMethods() {
    return Collections.unmodifiableSet(methods);
  }
  
  public void addMethod(Method method) {
    this.methods.stream().filter(sm -> StringUtils.equals(sm.getHttpMethod(), method.getHttpMethod())).findAny()
       .ifPresentOrElse(existing -> existing.merge(method), () -> methods.add(new SwaggerMethod(method)));
  }

  public String getLabel() {
    return resourceGroup.getLabel();
  }

  public String getDeprecated() {
    return resourceGroup.getDeprecated();
  }
}
