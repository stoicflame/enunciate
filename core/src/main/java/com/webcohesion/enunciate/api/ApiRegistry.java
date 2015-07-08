package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.ServiceGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ApiRegistry {

  private final List<? extends ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
  private final List<? extends ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
  private final List<Syntax> syntaxes = new ArrayList<Syntax>();

  public List<? extends ServiceGroup> getServiceGroups() {
    return serviceGroups;
  }

  public List<? extends ResourceGroup> getResourceGroups() {
    return resourceGroups;
  }

  public List<Syntax> getSyntaxes() {
    return syntaxes;
  }
}
