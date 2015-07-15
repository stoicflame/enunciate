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

  private final List<ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
  private final List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
  private final List<Syntax> syntaxes = new ArrayList<Syntax>();

  public List<ServiceGroup> getServiceGroups() {
    return serviceGroups;
  }

  public List<ResourceGroup> getResourceGroups() {
    return resourceGroups;
  }

  public List<Syntax> getSyntaxes() {
    return syntaxes;
  }
}
