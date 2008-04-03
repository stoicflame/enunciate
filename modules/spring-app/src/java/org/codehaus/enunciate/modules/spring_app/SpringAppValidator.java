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

package org.codehaus.enunciate.modules.spring_app;

import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import java.util.HashMap;

/**
 * Validator for the xfire server.
 *
 * @author Ryan Heaton
 */
public class SpringAppValidator extends BaseValidator {

  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (!visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        // todo: support multiple versions of web services.
        result.addError(ei.getPosition(), "Enunciate doesn't support two endpoint interfaces with the same service name, " +
          "even though they have different namespaces.  Future support for this is pending...");
      }
    }

    return result;
  }

}
