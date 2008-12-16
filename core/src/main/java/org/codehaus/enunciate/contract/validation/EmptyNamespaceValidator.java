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

package org.codehaus.enunciate.contract.validation;

import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMessage;
import org.codehaus.enunciate.contract.jaxws.WebMessagePart;
import org.codehaus.enunciate.contract.jaxws.WebMethod;

/**
 * Validator that ensures no API elements are in the empty namespace.
 *
 * @author Ryan Heaton
 */
public class EmptyNamespaceValidator extends BaseValidator {

  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();
    String ns = ei.getTargetNamespace();
    if (ns == null || "".equals(ns)) {
      result.addError(ei, "Endpoint interface is in the empty namespace.");
    }

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage message : webMethod.getMessages()) {
        for (WebMessagePart messagePart : message.getParts()) {
          if (messagePart.isImplicitSchemaElement()) {
            ns = messagePart.getParticleQName().getNamespaceURI();
            if (ns == null || "".equals(ns)) {
              result.addError(webMethod, "A particle for a message part (message: " + message.getMessageName() + ", part: " + messagePart.getPartName() + ") of this web method is in the empty namespace.");
            }
          }
        }
      }
    }

    return result;
  }

  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    return validateType(complexType);
  }

  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    return validateType(simpleType);
  }

  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    return validateType(enumType);
  }

  public ValidationResult validateType(TypeDefinition typeDefinition) {
    ValidationResult result = new ValidationResult();
    String ns = typeDefinition.getNamespace();
    if (ns == null || "".equals(ns)) {
      result.addError(typeDefinition, "Type is defined in the empty namespace.");
    }
    return result;
  }

  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    ValidationResult result = new ValidationResult();
    String ns = rootElementDeclaration.getNamespace();
    if (ns == null || "".equals(ns)) {
      result.addError(rootElementDeclaration, "Element is defined in the empty namespace.");
    }
    return result;
  }
}
