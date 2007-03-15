/*
 * Copyright 2006 Web Cohesion
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

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.EnumTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTMethod;

import java.util.Map;
import java.util.List;

/**
 * A validator that doesn't do any validation work.
 *
 * @author Ryan Heaton
 */
public class BaseValidator implements Validator {

  /**
   * @return An empty result.
   */
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    return new ValidationResult();
  }

  /**
   * @return An empty result. @param restAPI
   */
  public ValidationResult validateRESTAPI(Map<String, List<RESTMethod>> restAPI) {
    return new ValidationResult();
  }

  /**
   * @return An empty result.
   */
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    return new ValidationResult();
  }

  /**
   * @return An empty result.
   */
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    return new ValidationResult();
  }

  /**
   * @return An empty result.
   */
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    return new ValidationResult();
  }

  /**
   * @return An empty result.
   */
  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    return new ValidationResult();
  }

}
