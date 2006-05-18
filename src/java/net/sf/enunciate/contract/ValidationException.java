package net.sf.enunciate.contract;

/**
 * A validation exception is thrown if any validation errors occur during validation.
 *
 * @author Ryan Heaton
 */
public class ValidationException extends RuntimeException {

  public ValidationException(String message) {
    super(message);
  }

}
