package net.sf.enunciate.contract.jaxws.validation;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxws.*;

/**
 * Validator for JAX-WS contract implementation structures.
 *
 * @author Ryan Heaton
 */
public interface JAXWSValidator {

  /**
   * Whether a given type declaration is an endpoint implementation.
   *
   * @param declaration The declaration to determine whether it is an endpoint implementation.
   * @return Whether a given type declaration is an endpoint implementation.
   */
  boolean isEndpointImplementation(TypeDeclaration declaration);

  /**
   * Whether a given type declaration is an endpoint interface.
   *
   * @param declaration The declaration to determine whether it is an endpoint interface.
   * @return Whether a given type declaration is an endpoint interface.
   */
  boolean isEndpointInterface(TypeDeclaration declaration);

  /**
   * Whether the given method declaration is a web method.
   *
   * @param declaration The declaration to determine whether it is a web method.
   * @return Whether the given method declaration is a web method.
   */
  boolean isWebMethod(MethodDeclaration declaration);

  /**
   * Validates an endpoint implementation.
   *
   * @param impl The endpoint implementation to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(EndpointImplementation impl) throws ValidationException;

  /**
   * Validates an endpoint interface.
   *
   * @param ei The endpoint interface to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(EndpointInterface ei) throws ValidationException;

  /**
   * Validates a web method.
   *
   * @param webMethod The web method to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(WebMethod webMethod) throws ValidationException;

  /**
   * Validates a request wrapper.
   *
   * @param requestWrapper The request wrapper to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(RequestWrapper requestWrapper) throws ValidationException;

  /**
   * Validates a response wrapper.
   *
   * @param responseWrapper The response wrapper to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(ResponseWrapper responseWrapper) throws ValidationException;

  /**
   * Validates a web param.
   *
   * @param webParam The web param to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(WebParam webParam) throws ValidationException;

  /**
   * Validates a web result.
   *
   * @param webResult The web result to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(WebResult webResult) throws ValidationException;

  /**
   * Validates a web fault.
   *
   * @param webFault The web fault to validate.
   * @throws ValidationException If there were validation errors.
   */
  void validate(WebFault webFault) throws ValidationException;
}
