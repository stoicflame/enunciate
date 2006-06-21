package net.sf.enunciate.contract.validation;

import com.sun.mirror.util.SourcePosition;

/**
 * @author Ryan Heaton
 */
public class ValidationMessage {

  private SourcePosition position;
  private String text;

  public ValidationMessage(SourcePosition position, String text) {
    this.position = position;
    this.text = text;
  }

  /**
   * The source position of this validation message.
   *
   * @return The source position of this validation message.
   */
  public SourcePosition getPosition() {
    return position;
  }

  /**
   * The text of the message.
   *
   * @return The text of the message.
   */
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return getPosition() + ": " + getText();
  }


}
