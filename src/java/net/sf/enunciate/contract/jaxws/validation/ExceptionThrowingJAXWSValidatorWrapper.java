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
  public ValidationResult validate(EndpointImplementation impl) throws ValidationException {
    ValidationResult result = delegate.validate(impl);
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
  public ValidationResult validate(EndpointInterface ei) throws ValidationException {
    ValidationResult result = delegate.validate(ei);
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
  public ValidationResult validate(WebMethod webMethod) throws ValidationException {
    ValidationResult result = delegate.validate(webMethod);
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
  public ValidationResult validate(RequestWrapper requestWrapper) throws ValidationException {
    ValidationResult result = delegate.validate(requestWrapper);
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
  public ValidationResult validate(ResponseWrapper responseWrapper) throws ValidationException {
    ValidationResult result = delegate.validate(responseWrapper);
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
  public ValidationResult validate(WebParam webParam) throws ValidationException {
    ValidationResult result = delegate.validate(webParam);
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
  public ValidationResult validate(WebResult webResult) throws ValidationException {
    ValidationResult result = delegate.validate(webResult);
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
  public ValidationResult validate(WebFault webFault) throws ValidationException {
    ValidationResult result = delegate.validate(webFault);
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
