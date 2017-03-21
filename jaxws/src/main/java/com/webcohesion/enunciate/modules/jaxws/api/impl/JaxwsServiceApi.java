package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;
import com.webcohesion.enunciate.modules.jaxws.WsdlInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JaxwsServiceApi implements ServiceApi {

  private final EnunciateJaxwsContext context;
  private ApiRegistrationContext registrationContext;

  public JaxwsServiceApi(EnunciateJaxwsContext context, ApiRegistrationContext registrationContext) {
    this.context = context;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getContextPath() {
    return this.context.getContextPath();
  }

  @Override
  public List<ServiceGroup> getServiceGroups() {
    Map<String, WsdlInfo> wsdls = this.context.getWsdls();
    ArrayList<ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
    for (WsdlInfo wsdlInfo : wsdls.values()) {
      serviceGroups.add(new ServiceGroupImpl(wsdlInfo, registrationContext));
    }
    return serviceGroups;
  }
}
