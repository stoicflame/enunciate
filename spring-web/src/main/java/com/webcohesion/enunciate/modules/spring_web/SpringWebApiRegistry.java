package com.webcohesion.enunciate.modules.spring_web;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.modules.spring_web.api.impl.SpringWebResourceApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class SpringWebApiRegistry implements ApiRegistry {

  private final EnunciateSpringWebContext context;

  public SpringWebApiRegistry(EnunciateSpringWebContext context) {
    this.context = context;
  }

  @Override
  public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
    return Collections.emptyList();
  }

  @Override
  public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
    return this.context.getControllers().isEmpty() ? Collections.<ResourceApi>emptyList() : Collections.singletonList((ResourceApi) new SpringWebResourceApi(this.context, context));
  }

  @Override
  public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
    return Collections.emptySet();
  }

  @Override
  public InterfaceDescriptionFile getSwaggerUI() {
    return null;
  }
}
