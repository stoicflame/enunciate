package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.JaxrsResourceApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class JaxrsApiRegistry implements ApiRegistry {

  private final EnunciateJaxrsContext context;

  public JaxrsApiRegistry(EnunciateJaxrsContext context) {
    this.context = context;
  }

  @Override
  public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
    return Collections.emptyList();
  }

  @Override
  public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
    return this.context.getRootResources().isEmpty() ? Collections.<ResourceApi>emptyList() : Collections.<ResourceApi>singletonList(new JaxrsResourceApi(this.context, context));
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
