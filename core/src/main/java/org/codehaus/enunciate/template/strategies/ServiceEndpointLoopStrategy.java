/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.template.strategies;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.ServiceEndpoint;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;
import org.codehaus.enunciate.contract.rest.RESTMethod;

import java.util.*;

/**
 * Iterates through each unique service endpoint interface.
 *
 * @author Ryan Heaton
 */
public class ServiceEndpointLoopStrategy extends EnunciateTemplateLoopStrategy<ServiceEndpoint> {

  private String var = "endpoint";

  protected Iterator<ServiceEndpoint> getLoop(TemplateModel model) throws TemplateException {
    Set<ServiceEndpoint> interfaces = new TreeSet<ServiceEndpoint>(new Comparator<ServiceEndpoint>() {
      public int compare(ServiceEndpoint e1, ServiceEndpoint e2) {
        return e1.getServiceEndpointId().compareTo(e2.getServiceEndpointId());
      }
    });

    for (RESTEndpoint restEndpoint : getRESTEndpoints()) {
      for (RESTMethod restMethod : restEndpoint.getRESTMethods()) {
        interfaces.add(restMethod);
      }
    }

    Collection<WsdlInfo> wsdls = getNamespacesToWSDLs().values();
    for (WsdlInfo wsdl : wsdls) {
      for (EndpointInterface endpointInterface : wsdl.getEndpointInterfaces()) {
        for (EndpointImplementation impl : endpointInterface.getEndpointImplementations()) {
          interfaces.add(impl);
        }
      }
    }

    return interfaces.iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, ServiceEndpoint endpoint, int index) throws TemplateException {
    super.setupModelForLoop(model, endpoint, index);

    if (this.var != null) {
      getModel().setVariable(var, endpoint);
    }
  }

  /**
   * The variable into which to store the current endpoint interface.
   *
   * @return The variable into which to store the current endpoint interface.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current endpoint interface.
   *
   * @param var The variable into which to store the current endpoint interface.
   */
  public void setVar(String var) {
    this.var = var;
  }}
