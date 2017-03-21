package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
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
}
