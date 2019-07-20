package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.module.ApiRegistryProviderModule;
import com.webcohesion.enunciate.module.EnunciateModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class AggregatedApiRegistry implements ApiRegistry {

  private final Enunciate enunciate;

  public AggregatedApiRegistry(Enunciate enunciate) {
    this.enunciate = enunciate;
  }

  @Override
  public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
    ArrayList<ServiceApi> serviceApis = new ArrayList<ServiceApi>();
    List<EnunciateModule> modules = enunciate.getModules();
    for (EnunciateModule module : modules) {
      if (module.isEnabled() && module instanceof ApiRegistryProviderModule) {
        serviceApis.addAll(((ApiRegistryProviderModule) module).getApiRegistry().getServiceApis(context));
      }
    }
    return serviceApis;
  }

  @Override
  public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
    ArrayList<ResourceApi> resourceApis = new ArrayList<ResourceApi>();
    List<EnunciateModule> modules = enunciate.getModules();
    for (EnunciateModule module : modules) {
      if (module.isEnabled() && module instanceof ApiRegistryProviderModule) {
        resourceApis.addAll(((ApiRegistryProviderModule) module).getApiRegistry().getResourceApis(context));
      }
    }
    return resourceApis;
  }

  @Override
  public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
    Set<Syntax> syntaxes = new TreeSet<Syntax>();
    List<EnunciateModule> modules = enunciate.getModules();
    for (EnunciateModule module : modules) {
      if (module.isEnabled() && module instanceof ApiRegistryProviderModule) {
        syntaxes.addAll(((ApiRegistryProviderModule) module).getApiRegistry().getSyntaxes(context));
      }
    }
    return syntaxes;
  }

  @Override
  public InterfaceDescriptionFile getSwaggerUI() {
    List<EnunciateModule> modules = enunciate.getModules();
    for (EnunciateModule module : modules) {
      if (module.isEnabled() && module instanceof ApiRegistryProviderModule) {
        InterfaceDescriptionFile swaggerUI = ((ApiRegistryProviderModule) module).getApiRegistry().getSwaggerUI();
        if (swaggerUI != null) {
          return swaggerUI;
        }
      }
    }
    return null;
  }

}
