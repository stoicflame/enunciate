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

import com.sun.mirror.declaration.MethodDeclaration;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import javax.jws.WebService;
import java.util.HashMap;

/**
 * Validator for the cxf module.
 *
 * @author Ryan Heaton
 */
public class CXFValidator extends BaseValidator {

  private final boolean enableJaxws;
  private final boolean enableJaxrs;
  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  public CXFValidator(boolean enableJaxws, boolean enableJaxrs) {
    this.enableJaxws = enableJaxws;
    this.enableJaxrs = enableJaxrs;
  }

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    if (enableJaxws) {
      EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
      if (visited != null) {
        if (visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
          result.addError(ei, "Ummm... you already have a service named " + ei.getServiceName() + " at " +
            visited.getPosition() + ".  You need to disambiguate.");
        }
      }

      if (ei.isInterface()) {
        WebService eiAnnotation = ei.getAnnotation(WebService.class);
        if (!"".equals(eiAnnotation.serviceName())) {
          result.addError(ei, "CXF fails if you specify 'serviceName' on an endpoint interface.");
        }
        if (!"".equals(eiAnnotation.portName())) {
          result.addError(ei, "CXF fails if you specify 'portName' on an endpoint interface.");
        }
        for (MethodDeclaration m : ei.getMethods()) {
          javax.jws.WebMethod wm = m.getAnnotation(javax.jws.WebMethod.class);
          if (wm != null && wm.exclude()) {
            result.addError(m, "CXF fails if you specify 'exclude=true' on an endpoint interface.");
          }
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
          if (webFault.getExplicitFaultBeanType() != null) {
            ElementDeclaration faultBean = webFault.findExplicitFaultBean();
            if (!webFault.getSimpleName().equals(faultBean.getName())) {
              result.addError(webMethod, "Because of some inconsistencies with the JAX-WS implementation of CXF, the CXF module cannot have methods that " +
                "throw exceptions that are named differently than the element name of their explicit fault beans. Apply @XmlRootElement ( name = \""
                + webFault.getSimpleName() + "\" ) to  explicit fault bean " + webFault.getExplicitFaultBeanType() + " to apply the workaround.");
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
    }

    return result;
  }

}
