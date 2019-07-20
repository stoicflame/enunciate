package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ServiceGroupImpl implements ServiceGroup {

  private final WsdlInfo wsdlInfo;
  private final ApiRegistrationContext registrationContext;

  public ServiceGroupImpl(WsdlInfo wsdlInfo, ApiRegistrationContext registrationContext) {
    this.wsdlInfo = wsdlInfo;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getNamespace() {
    return this.wsdlInfo.getTargetNamespace();
  }

  @Override
  public InterfaceDescriptionFile getWsdlFile() {
    return this.wsdlInfo.getWsdlFile();
  }

  @Override
  public List<? extends Service> getServices() {
    ArrayList<Service> services = new ArrayList<Service>();
    FacetFilter facetFilter = this.registrationContext.getFacetFilter();
    for (EndpointInterface endpointInterface : this.wsdlInfo.getEndpointInterfaces()) {
      if (!facetFilter.accept(endpointInterface)) {
        continue;
      }

      services.add(new ServiceImpl(endpointInterface, "", registrationContext));
    }
    Collections.sort(services, new Comparator<Service>() {
      @Override
      public int compare(Service o1, Service o2) {
        return o1.getLabel().compareTo(o2.getLabel());
      }
    });
    return services;
  }
}
