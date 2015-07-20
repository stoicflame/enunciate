package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitSchemaElement;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebMessage;
import com.webcohesion.enunciate.modules.jaxws.model.WebMessagePart;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;

import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxwsContext extends EnunciateModuleContext implements ServiceApi {

  private final EnunciateJaxbContext jaxbContext;
  private final boolean forceJAXWSSpecCompliance;
  private final Map<String, WsdlInfo> wsdls = new HashMap<String, WsdlInfo>();
  private final List<EndpointInterface> endpointInterfaces = new ArrayList<EndpointInterface>();

  public EnunciateJaxwsContext(EnunciateJaxbContext jaxbContext, boolean forceJAXWSSpecCompliance) {
    super(jaxbContext.getContext());
    this.jaxbContext = jaxbContext;
    this.forceJAXWSSpecCompliance = forceJAXWSSpecCompliance;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public EnunciateJaxbContext getJaxbContext() {
    return jaxbContext;
  }

  public boolean isForceJAXWSSpecCompliance() {
    return forceJAXWSSpecCompliance;
  }

  public List<EndpointInterface> getEndpointInterfaces() {
    return endpointInterfaces;
  }

  public Map<String, WsdlInfo> getWsdls() {
    return wsdls;
  }

  /**
   * Add an endpoint interface to the model.
   *
   * @param ei The endpoint interface to add to the model.
   */
  public void add(EndpointInterface ei) {
    String namespace = ei.getTargetNamespace();

    String prefix = this.jaxbContext.addNamespace(namespace);

    WsdlInfo wsdlInfo = wsdls.get(namespace);
    if (wsdlInfo == null) {
      wsdlInfo = new WsdlInfo(jaxbContext);
      wsdlInfo.setId(prefix);
      wsdls.put(namespace, wsdlInfo);
      wsdlInfo.setTargetNamespace(namespace);
    }

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        for (WebMessagePart messagePart : webMessage.getParts()) {
          if (messagePart.isImplicitSchemaElement()) {
            ImplicitSchemaElement implicitElement = (ImplicitSchemaElement) messagePart;
            String particleNamespace = messagePart.getParticleQName().getNamespaceURI();
            SchemaInfo schemaInfo = this.jaxbContext.getSchemas().get(particleNamespace);
            if (schemaInfo == null) {
              schemaInfo = new SchemaInfo(this.jaxbContext);
              schemaInfo.setId(this.jaxbContext.addNamespace(particleNamespace));
              schemaInfo.setNamespace(particleNamespace);
              this.jaxbContext.getSchemas().put(particleNamespace, schemaInfo);
            }
            schemaInfo.getImplicitSchemaElements().add(implicitElement);
          }
        }
      }
    }

    wsdlInfo.getEndpointInterfaces().add(ei);
    this.endpointInterfaces.add(ei);
    debug("Added %s as a JAX-WS endpoint interface.", ei.getQualifiedName());
  }

  @Override
  public String getContextPath() {
    return "";
  }

  @Override
  public List<ServiceGroup> getServiceGroups() {
    Map<String, WsdlInfo> wsdls = getWsdls();
    ArrayList<ServiceGroup> serviceGroups = new ArrayList<ServiceGroup>();
    for (WsdlInfo wsdlInfo : wsdls.values()) {
      serviceGroups.add(wsdlInfo);
    }
    return serviceGroups;
  }
}
