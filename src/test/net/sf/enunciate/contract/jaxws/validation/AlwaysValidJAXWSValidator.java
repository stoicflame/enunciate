package net.sf.enunciate.contract.jaxws.validation;

import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.*;

/**
 * @author Ryan Heaton
 */
public class AlwaysValidJAXWSValidator implements JAXWSValidator {

  public ValidationResult validateEndpointImplementation(EndpointImplementation impl) {
    return new ValidationResult();
  }

  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    return new ValidationResult();
  }

  public ValidationResult validateWebMethod(WebMethod webMethod) {
    return new ValidationResult();
  }

  public ValidationResult validateRequestWrapper(RequestWrapper requestWrapper) {
    return new ValidationResult();
  }

  public ValidationResult validateResponseWrapper(ResponseWrapper responseWrapper) {
    return new ValidationResult();
  }

  public ValidationResult validateWebParam(WebParam webParam) {
    return new ValidationResult();
  }

  public ValidationResult validateWebResult(WebResult webResult) {
    return new ValidationResult();
  }

  public ValidationResult validateWebFault(WebFault webFault) {
    return new ValidationResult();
  }
}
