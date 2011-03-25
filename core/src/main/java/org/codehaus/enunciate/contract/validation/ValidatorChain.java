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
import org.codehaus.enunciate.contract.jaxrs.RootResource;

import java.util.*;

/**
 * Chains a set of validators.
 *
 * @author Ryan Heaton
 */
public class ValidatorChain implements Validator {

  private final Map<String, Validator> validators = new LinkedHashMap<String, Validator>();

  public ValidatorChain() {
  }

  /**
   * The list of validators in the chain.
   *
   * @return The list of validators in the chain.
   * @deprecated Use the getValidatorsByLabel() method.
   */
  public List<Validator> getValidators() {
    return Collections.list(Collections.enumeration(validators.values()));
  }

  /**
   * Get a map of labels-to-validators.
   *
   * @return The map of validators-to-labels.
   */
  public Map<String, Validator> getValidatorsByLabel() {
    return Collections.unmodifiableMap(validators);
  }

  /**
   * Adds a validator.
   *
   * @param label The label of the validator.
   * @param validator The validator.
   */
  public void addValidator(String label, Validator validator) {
    this.validators.put(label, validator);
  }

  // Inherited.
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateEndpointInterface(ei));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateRootResources(List<RootResource> rootResources) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateRootResources(rootResources));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateRootElement(rootElementDeclaration));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateComplexType(complexType));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateSimpleType(simpleType));
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = new ValidationResult();

    for (Map.Entry<String, Validator> validatorEntry : validators.entrySet()) {
      result.aggregate(validatorEntry.getKey(), validatorEntry.getValue().validateEnumType(enumType));
    }

    return result;
  }

}
