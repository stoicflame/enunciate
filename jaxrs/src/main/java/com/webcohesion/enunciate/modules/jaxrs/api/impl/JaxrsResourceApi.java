package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.resources.*;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class JaxrsResourceApi implements ResourceApi {

  private final EnunciateJaxrsContext context;
  private final ApiRegistrationContext registrationContext;

  public JaxrsResourceApi(EnunciateJaxrsContext context, ApiRegistrationContext registrationContext) {
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
            if (methodname.equals(((MethodImpl) method).getResourceMethod().getSimpleName().toString()) && ((MethodImpl)method).getResourceMethod().getParent().getQualifiedName().contentEquals(classname)) {
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
            if (((MethodImpl)method).getResourceMethod().getParent().getQualifiedName().contentEquals(classname)) {
              return resourceGroup;
            }
          }
        }
      }
    }
    return null;
  }
}
