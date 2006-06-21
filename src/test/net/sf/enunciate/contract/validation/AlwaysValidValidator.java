package net.sf.enunciate.contract.validation;

import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;

/**
 * @author Ryan Heaton
 */
public class AlwaysValidValidator implements Validator {

  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    return new ValidationResult();
  }

  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    return new ValidationResult();
  }

  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    return new ValidationResult();
  }

  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    return new ValidationResult();
  }

  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    return new ValidationResult();
  }

}
