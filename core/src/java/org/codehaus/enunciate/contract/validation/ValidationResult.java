package org.codehaus.enunciate.contract.validation;

import com.sun.mirror.util.SourcePosition;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of validation.
 *
 * @author Ryan Heaton
 */
public class ValidationResult {

  private final List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
  private final List<ValidationMessage> warnings = new ArrayList<ValidationMessage>();

  /**
   * Whether there are any errors in the result.
   *
   * @return Whether there are any errors in the result.
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /**
   * Add an error message.
   *
   * @param position The source position.
   * @param text     The text of the error message.
   */
  public void addError(SourcePosition position, String text) {
    this.errors.add(new ValidationMessage(position, text));
  }

  /**
   * The errors.
   *
   * @return The errors.
   */
  public List<ValidationMessage> getErrors() {
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
   * Add an warning message.
   *
   * @param position The source position.
   * @param text     The text of the warning message.
   */
  public void addWarning(SourcePosition position, String text) {
    this.warnings.add(new ValidationMessage(position, text));
  }

  /**
   * Whether there are any warnings in the result.
   *
   * @return Whether there are any warnings in the result.
   */
  public List<ValidationMessage> getWarnings() {
    return warnings;
  }

  /**
   * Aggregate the specified result to these results.
   *
   * @param result The result to aggregate.
   */
  public void aggregate(ValidationResult result) {
    this.errors.addAll(result.errors);
    this.warnings.addAll(result.warnings);
  }

}
