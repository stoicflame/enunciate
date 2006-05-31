package net.sf.enunciate.contract.jaxb.validation;

import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;

/**
 * A validator for JAXB contract implementation structures.
 *
 * @author Ryan Heaton
 */
public interface JAXBValidator {

  /**
   * Validate a complex type definition.
   *
   * @param complexType The complex type to validate.
   * @return The results of the validation.
   */
  ValidationResult validate(ComplexTypeDefinition complexType);

  /**
   * Valiate a simple type definition.
   *
   * @param simpleType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validate(SimpleTypeDefinition simpleType);

  /**
   * Valiate an enum type definition.
   *
   * @param enumType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validate(EnumTypeDefinition enumType);

  /**
   * Validate a global element declaration.
   *
   * @param rootElementDeclaration The global element declaration.
   * @return The results of the validation.
   */
  ValidationResult validate(RootElementDeclaration rootElementDeclaration);
}
