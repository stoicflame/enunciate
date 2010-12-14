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

package org.codehaus.enunciate.modules.xml;

import org.codehaus.enunciate.contract.jaxb.ImplicitSchemaElement;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import java.util.HashMap;

/**
 * Validator for the xml module.
 *
 * @author Ryan Heaton
 */
public class XMLValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    HashMap<String, WebMessagePart> implicitElementNames = new HashMap<String, WebMessagePart>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        for (WebMessagePart webMessagePart : webMessage.getParts()) {
          if (!(webMessagePart instanceof WebFault) && (webMessagePart.isImplicitSchemaElement())) {
            ImplicitSchemaElement el = ((ImplicitSchemaElement) webMessagePart);
            WebMessagePart otherPart = implicitElementNames.put(el.getElementName(), webMessagePart);
            if (otherPart != null && ((ImplicitSchemaElement)otherPart).getTypeQName() != null && !((ImplicitSchemaElement)otherPart).getTypeQName().equals(el.getTypeQName())) {
              result.addError(webMethod, "Web method defines a message part named '" + el.getElementName() +
                "' that is identical to the name of a web message part defined in " + otherPart.getWebMethod().getPosition() + ".  Please use annotations to disambiguate.");
            }
          }
        }
      }
    }

    return result;
  }

}
