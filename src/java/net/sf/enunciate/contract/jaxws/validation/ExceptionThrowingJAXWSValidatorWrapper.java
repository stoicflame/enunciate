package net.sf.enunciate.contract.jaxws.validation;

import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.jelly.apt.Context;

/**
 * A validator that throws <code>ValidationException</code>s (instead of returning the result) if any validation errors
 * have been accrued and logs any warnings.
 *
 * @author Ryan Heaton
 */
public class ExceptionThrowingJAXWSValidatorWrapper implements JAXWSValidator {

  private final JAXWSValidator delegate;

  public ExceptionThrowingJAXWSValidatorWrapper(JAXWSValidator delegate) {
    this.delegate = delegate;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateEndpointImplementation(EndpointImplementation impl) throws ValidationException {
    ValidationResult result = delegate.validateEndpointImplementation(impl);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(impl.getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateEndpointInterface(EndpointInterface ei) throws ValidationException {
    ValidationResult result = delegate.validateEndpointInterface(ei);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(ei.getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateWebMethod(WebMethod webMethod) throws ValidationException {
    ValidationResult result = delegate.validateWebMethod(webMethod);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(webMethod.getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateRequestWrapper(RequestWrapper requestWrapper) throws ValidationException {
    ValidationResult result = delegate.validateRequestWrapper(requestWrapper);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(requestWrapper.getWebMethod().getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateResponseWrapper(ResponseWrapper responseWrapper) throws ValidationException {
    ValidationResult result = delegate.validateResponseWrapper(responseWrapper);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(responseWrapper.getWebMethod().getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateWebParam(WebParam webParam) throws ValidationException {
    ValidationResult result = delegate.validateWebParam(webParam);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(webParam.getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateWebResult(WebResult webResult) throws ValidationException {
    ValidationResult result = delegate.validateWebResult(webResult);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(webResult.getWebMethod().getPosition(), result.getErrors().get(0));
    }

    return result;
  }

  /**
   * Calls the delegate and throws an exception if there are any errors.
   *
   * @throws ValidationException If there are any errors.
   */
  public ValidationResult validateWebFault(WebFault webFault) throws ValidationException {
    ValidationResult result = delegate.validateWebFault(webFault);
    if (result.hasWarnings()) {
      for (String warning : result.getWarnings()) {
        Context.getCurrentEnvironment().getMessager().printWarning(warning);
      }
    }

    if (result.hasErrors()) {
      throw new ValidationException(webFault.getPosition(), result.getErrors().get(0));
    }

    return result;
  }

}
