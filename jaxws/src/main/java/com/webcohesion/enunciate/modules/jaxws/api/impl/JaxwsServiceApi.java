package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Service;
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

  @Override
  public Operation findOperationFor(String classname, String method) {
    if (method.isEmpty() || classname.isEmpty()) {
      return null;
    }

    for (ServiceGroup serviceGroup : getServiceGroups()) {
      for (Service service : serviceGroup.getServices()) {
        for (Operation operation : service.getOperations()) {
          if (operation instanceof OperationImpl) {
            if (method.startsWith(((OperationImpl)operation).getWebMethod().getSimpleName().toString()) && ((OperationImpl)operation).getWebMethod().getDeclaringEndpointInterface().getQualifiedName().contentEquals(classname)) {
              return operation;
            }
          }
        }
      }
    }

    return null;
  }

  @Override
  public Service findServiceFor(String classname) {
    if (classname.isEmpty()) {
      return null;
    }
    for (ServiceGroup serviceGroup : getServiceGroups()) {
      for (Service service : serviceGroup.getServices()) {
        for (Operation operation : service.getOperations()) {
          if (operation instanceof OperationImpl) {
            if (((OperationImpl)operation).getWebMethod().getDeclaringEndpointInterface().getQualifiedName().contentEquals(classname)) {
              return service;
            }
          }
        }
      }
    }

    return null;
  }
}
