package net.sf.enunciate.contract.jaxws.validation;

import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.*;

/**
 * Validator for JAX-WS contract implementation structures.
 *
 * @author Ryan Heaton
 */
public interface JAXWSValidator {

  /**
   * Validates an endpoint implementation.
   *
   * @param impl The endpoint implementation to validate.
   */
  ValidationResult validate(EndpointImplementation impl);

  /**
   * Validates an endpoint interface.
   *
   * @param ei The endpoint interface to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(EndpointInterface ei);

  /**
   * Validates a web method.
   *
   * @param webMethod The web method to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(WebMethod webMethod);

  /**
   * Validates a request wrapper.
   *
   * @param requestWrapper The request wrapper to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(RequestWrapper requestWrapper);

  /**
   * Validates a response wrapper.
   *
   * @param responseWrapper The response wrapper to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(ResponseWrapper responseWrapper);

  /**
   * Validates a web param.
   *
   * @param webParam The web param to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(WebParam webParam);

  /**
   * Validates a web result.
   *
   * @param webResult The web result to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(WebResult webResult);

  /**
   * Validates a web fault.
   *
   * @param webFault The web fault to validate.
   * @throws ValidationException If there were validation errors.
   */
  ValidationResult validate(WebFault webFault);
}
