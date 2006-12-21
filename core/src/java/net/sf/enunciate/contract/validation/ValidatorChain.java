package net.sf.enunciate.contract.validation;

import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.rest.RESTMethod;

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
