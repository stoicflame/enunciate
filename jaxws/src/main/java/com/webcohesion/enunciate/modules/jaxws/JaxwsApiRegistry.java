package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.modules.jaxb.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jaxws.api.impl.JaxwsServiceApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class JaxwsApiRegistry implements ApiRegistry {

  private final EnunciateJaxwsContext context;

  public JaxwsApiRegistry(EnunciateJaxwsContext context) {
    this.context = context;
  }

  @Override
  public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
    return this.context.getEndpointInterfaces().isEmpty() ? Collections.<ServiceApi>emptyList() : Collections.<ServiceApi>singletonList(new JaxwsServiceApi(this.context, context));
  }

  @Override
  public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
    return Collections.emptyList();
  }

  @Override
  public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
    return this.context.getEndpointInterfaces().isEmpty() ? Collections.<Syntax>emptySet() : Collections.<Syntax>singleton(new SyntaxImpl(this.context.getJaxbContext(), context));
  }

  @Override
  public InterfaceDescriptionFile getSwaggerUI() {
    return null;
  }
}
