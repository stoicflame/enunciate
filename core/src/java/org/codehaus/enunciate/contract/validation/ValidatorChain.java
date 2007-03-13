package org.codehaus.enunciate.contract.validation;

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.EnumTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTMethod;

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
  public ValidationResult validateRESTAPI(Map<String, List<RESTMethod>> restAPI) {
    ValidationResult result = new ValidationResult();

    for (Validator validator : validators) {
      result.aggregate(validator.validateRESTAPI(restAPI));
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
