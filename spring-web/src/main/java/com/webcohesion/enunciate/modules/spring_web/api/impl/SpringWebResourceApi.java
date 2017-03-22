package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class SpringWebResourceApi implements ResourceApi {

  private final EnunciateSpringWebContext context;
  private final ApiRegistrationContext registrationContext;

  public SpringWebResourceApi(EnunciateSpringWebContext context, ApiRegistrationContext registrationContext) {
    this.context = context;
    this.registrationContext = registrationContext;
  }

  @Override
  public boolean isIncludeResourceGroupName() {
    return this.context.isIncludeResourceGroupName();
  }

  @Override
  public InterfaceDescriptionFile getWadlFile() {
    return this.context.getWadlFile();
  }

  @Override
  public List<ResourceGroup> getResourceGroups() {
    return this.context.getResourceGroups(registrationContext);
  }

  @Override
  public Method findMethodFor(String classname, String methodname) {
    if (methodname.isEmpty() || classname.isEmpty()) {
      return null;
    }

    for (ResourceGroup resourceGroup : getResourceGroups()) {
      for (Resource resource : resourceGroup.getResources()) {
        for (Method method : resource.getMethods()) {
          if (method instanceof MethodImpl) {
            if (methodname.startsWith(((MethodImpl) method).getRequestMapping().getSimpleName().toString()) && ((MethodImpl)method).getRequestMapping().getParent().getQualifiedName().contentEquals(classname)) {
              return method;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public ResourceGroup findResourceGroupFor(String classname) {
    if (classname.isEmpty()) {
      return null;
    }

    for (ResourceGroup resourceGroup : getResourceGroups()) {
      for (Resource resource : resourceGroup.getResources()) {
        for (Method method : resource.getMethods()) {
          if (method instanceof MethodImpl) {
            if (((MethodImpl)method).getRequestMapping().getParent().getQualifiedName().contentEquals(classname)) {
              return resourceGroup;
            }
          }
        }
      }
    }
    return null;
  }
}
