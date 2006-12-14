package net.sf.enunciate.contract.validation;

import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;

/**
 * Validator for the contract classes.  A single validator will be assigned to one set of source classes.
 * This means that a validator may keep state without the fear that the methods will be called more than
 * once (or less than once, for that matter) per type declaration.
 *
 * @author Ryan Heaton
 */
public interface Validator {

  /**
   * Validates an endpoint interface.
   *
   * @param ei The endpoint interface to validate.
   * @return The result of the validation.
   */
  ValidationResult validateEndpointInterface(EndpointInterface ei);

  /**
   * Validate a complex type definition.
   *
   * @param complexType The complex type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateComplexType(ComplexTypeDefinition complexType);

  /**
   * Valiate a simple type definition.
   *
   * @param simpleType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateSimpleType(SimpleTypeDefinition simpleType);

  /**
   * Valiate an enum type definition.
   *
   * @param enumType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateEnumType(EnumTypeDefinition enumType);

  /**
   * Validate a global element declaration.
   *
   * @param rootElementDeclaration The global element declaration.
   * @return The results of the validation.
   */
  ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration);
}
