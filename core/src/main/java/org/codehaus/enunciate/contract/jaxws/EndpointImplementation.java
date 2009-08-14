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

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import org.codehaus.enunciate.ClientName;
import org.codehaus.enunciate.contract.ServiceEndpoint;

import javax.annotation.Resource;

/**
 * A class specified as a web service endpoint implementation.  Remember an endpoint implementation could
 * possibly implicitly define an endpoint interface (see spec, section 3.3).
 *
 * @author Ryan Heaton
 */
public class EndpointImplementation extends DecoratedClassDeclaration implements ServiceEndpoint {

  private final EndpointInterface endpointInterface;

  public EndpointImplementation(ClassDeclaration delegate, EndpointInterface endpointInterface) {
    super(delegate);

    this.endpointInterface = endpointInterface;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName();
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

  // Inherited.
  public TypeDeclaration getServiceEndpointInterface() {
    return getEndpointInterface();
  }

  // Inherited.
  public TypeDeclaration getServiceEndpointDefaultImplementation() {
    return this;
  }

}
