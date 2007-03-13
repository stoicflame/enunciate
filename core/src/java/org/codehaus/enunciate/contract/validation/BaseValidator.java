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
