package net.sf.enunciate.contract;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of validation.
 *
 * @author Ryan Heaton
 */
public class ValidationResult {

  private final List<String> errors = new ArrayList<String>();
  private final List<String> warnings = new ArrayList<String>();

  /**
   * Whether there are any errors in the result.
   *
   * @return Whether there are any errors in the result.
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /**
   * The errors.
   *
   * @return The errors.
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * Whether there are any warnings in the result.
   *
   * @return Whether there are any warnings in the result.
   */
  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }

  /**
   * Whether there are any warnings in the result.
   *
   * @return Whether there are any warnings in the result.
   */
  public List<String> getWarnings() {
    return warnings;
  }

}
