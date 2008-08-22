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

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.EnumTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.rest.RESTNoun;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.jaxrs.RootResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chains a set of validators.
 *
 * @author Ryan Heaton
 */
public class ValidatorChain implements Validator {

  private final ArrayList<Validator> validators = new ArrayList<Validator>();

  public ValidatorChain() {
  }

  /**
   * The list of validators in the chain.
   *
   * @return The list of validators in the chain.
   */
  public List<Validator> getValidators() {
    return validators;
  }

  /**
   * Adds a validator to the chain.
   *
   * @param validator The validator to add.
   */
  public void addValidator(Validator validator) {
    this.validators.add(validator);
  }

  // Inherited.
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateEndpointInterface(ei));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateRESTAPI(Map<RESTNoun, List<RESTMethod>> restAPI) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateRESTAPI(restAPI));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateRootResources(List<RootResource> rootResources) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateRootResources(rootResources));
    }

    return result;
  }

  public ValidationResult validateContentTypeHandlers(List<ContentTypeHandler> contentTypeHandlers) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateContentTypeHandlers(contentTypeHandlers));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateRootElement(rootElementDeclaration));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateComplexType(complexType));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateSimpleType(simpleType));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateEnumType(enumType));
    }

    return result;
  }

}
