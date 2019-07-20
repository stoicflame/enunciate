package com.webcohesion.enunciate.modules.jackson;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class JacksonApiRegistry implements ApiRegistry {

  private final EnunciateJacksonContext context;

  public JacksonApiRegistry(EnunciateJacksonContext context) {
    this.context = context;
  }

  @Override
  public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
    return Collections.emptyList();
  }

  @Override
  public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
    return Collections.emptyList();
  }

  @Override
  public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
    return this.context.getTypeDefinitions().isEmpty() ? Collections.<Syntax>emptySet() : Collections.<Syntax>singleton(new SyntaxImpl(this.context, context));
  }

  @Override
  public InterfaceDescriptionFile getSwaggerUI() {
    return null;
  }
}
