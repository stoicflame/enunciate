package net.sf.enunciate.contract;

import com.sun.mirror.util.SourcePosition;

/**
 * A validation exception is thrown if any validation errors occur during validation.
 *
 * @author Ryan Heaton
 */
public class ValidationException extends RuntimeException {

  public ValidationException(SourcePosition position, String message) {
    super(position.toString() + ": " + message);
  }

}
