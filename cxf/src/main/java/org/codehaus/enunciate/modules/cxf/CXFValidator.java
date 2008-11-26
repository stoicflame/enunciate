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

package org.codehaus.enunciate.modules.cxf;

import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import java.util.HashMap;

/**
 * Validator for the xfire server.
 *
 * @author Ryan Heaton
 */
public class CXFValidator extends BaseValidator {

  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        result.addError(ei, "Ummm... you already have a service named " + ei.getServiceName() + " at " +
          visited.getPosition() + ".  You need to disambiguate.");
      }
    }

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebParam webParam : webMethod.getWebParameters()) {
        if ((webParam.isHeader()) && ("".equals(webParam.getAnnotation(javax.jws.WebParam.class).name()))) {
          //todo: lift this constraint by serializing the parameter names to some file you can load for metadata...
          result.addError(webParam, "For now, Enunciate requires you to specify a 'name' on the @WebParam annotation if it's a header.");
        }
      }

      for (WebFault webFault : webMethod.getWebFaults()) {
        if (webFault.getExplicitFaultBean() != null) {
          if (!webFault.getSimpleName().equals(webFault.getExplicitFaultBean().getName())) {
            result.addError(webMethod, "Because of some inconsistencies with the JAX-WS implementation of CXF, the CXF module cannot have methods that " +
              "throw exceptions that are named differently than the element name of their explicit fault beans. Apply @XmlRootElement ( name = \""
              + webFault.getSimpleName() + "\" ) to  explicit fault bean " + webFault.getExplicitFaultBean().getQualifiedName() + " to apply the workaround.");
          }
        }
        else {
          String ns = webFault.getTargetNamespace() == null ? "" : webFault.getTargetNamespace();
          String eins = ei.getTargetNamespace() == null ? "" : ei.getTargetNamespace();
          if (!ns.equals(eins)) {
            result.addError(webMethod, "CXF doesn't handle throwing exceptions that are defined in a different namespace " +
              "from the namespace of the endpoint interface.");
          }
        }
      }
    }

    return result;
  }

}
