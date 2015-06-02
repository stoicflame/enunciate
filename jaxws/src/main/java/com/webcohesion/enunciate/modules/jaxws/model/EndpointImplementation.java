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

package com.webcohesion.enunciate.modules.jaxws.model;


import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;

import javax.annotation.Resource;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class specified as a web service endpoint implementation.  Remember an endpoint implementation could
 * possibly implicitly define an endpoint interface (see spec, section 3.3).
 *
 * @author Ryan Heaton
 */
public class EndpointImplementation extends DecoratedTypeElement implements HasFacets {

  private final EndpointInterface endpointInterface;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final EnunciateJaxwsContext context;

  public EndpointImplementation(TypeElement delegate, EndpointInterface endpointInterface, EnunciateJaxwsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    this.context = context;
    this.endpointInterface = endpointInterface;
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(endpointInterface.getFacets());
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName().toString();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

  /**
   * The endpoint interface specified for this web service.
   *
   * @return The endpoint interface specified for this web service
   */
  public EndpointInterface getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * Get the binding type for this endpoint implementation, or null if none is specified.
   *
   * @return The binding type for this endpoint implementation.
   */
  public BindingType getBindingType() {
    javax.xml.ws.BindingType bindingType = getAnnotation(javax.xml.ws.BindingType.class);

    if (bindingType != null) {
      if ((bindingType.value() != null) && (!"".equals(bindingType.value()))) {
        return BindingType.fromNamespace(bindingType.value());
      }
    }

    return BindingType.SOAP_1_1;
  }

  // Inherited.
  public String getServiceEndpointId() {
    String name = "enunciate:service:" + getSimpleName();
    Resource resource = getAnnotation(Resource.class);
    if (resource != null && !"".equals(resource.name())) {
      name = resource.name();
    }
    return name;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

}
