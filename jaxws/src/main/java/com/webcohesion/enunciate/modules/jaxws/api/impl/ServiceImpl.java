package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ServiceImpl implements Service {

  private final EndpointInterface ei;

  public ServiceImpl(EndpointInterface ei) {
    this.ei = ei;
  }


  @Override
  public String getLabel() {
    String serviceName = this.ei.getServiceName();
    if (serviceName.equals(this.ei.getSimpleName() + "Service")) {
      serviceName = this.ei.getSimpleName().toString();
    }
    return serviceName;
  }

  @Override
  public String getPath() {
    //todo: is this really the right path?
    return "/" + this.ei.getServiceName();
  }

  @Override
  public String getNamespace() {
    return this.ei.getTargetNamespace();
  }

  @Override
  public ServiceGroup getGroup() {
    return this.ei.getContext().getWsdls().get(this.ei.getTargetNamespace());
  }

  @Override
  public String getSlug() {
    return "service_" + this.ei.getContext().getJaxbContext().getNamespacePrefixes().get(this.ei.getTargetNamespace()) + "_" + this.ei.getServiceName();
  }

  @Override
  public String getDescription() {
    return this.ei.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return DecoratedElements.findDeprecationMessage(this.ei);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.ei.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.ei.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<? extends Operation> getOperations() {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    for (WebMethod webMethod : this.ei.getWebMethods()) {
      operations.add(new OperationImpl(webMethod, this));
    }
    return operations;
  }
}
