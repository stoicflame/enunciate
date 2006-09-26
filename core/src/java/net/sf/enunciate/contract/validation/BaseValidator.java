package net.sf.enunciate.contract.validation;

import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;

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
