package net.sf.enunciate.contract.jaxws.validation;

import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.*;

/**
 * @author Ryan Heaton
 */
public class AlwaysValidJAXWSValidator implements JAXWSValidator {

  public ValidationResult validate(EndpointImplementation impl) {
    return new ValidationResult();
  }

  public ValidationResult validate(EndpointInterface ei) {
    return new ValidationResult();
  }

  public ValidationResult validate(WebMethod webMethod) {
    return new ValidationResult();
  }

  public ValidationResult validate(RequestWrapper requestWrapper) {
    return new ValidationResult();
  }

  public ValidationResult validate(ResponseWrapper responseWrapper) {
    return new ValidationResult();
  }

  public ValidationResult validate(WebParam webParam) {
    return new ValidationResult();
  }

  public ValidationResult validate(WebResult webResult) {
    return new ValidationResult();
  }

  public ValidationResult validate(WebFault webFault) {
    return new ValidationResult();
  }
}
