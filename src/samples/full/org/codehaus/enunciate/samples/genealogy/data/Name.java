package org.codehaus.enunciate.samples.genealogy.data;

/**
 * A name assertion.
 *
 * @author Ryan Heaton
 */
public class Name extends Assertion {

  private String value;

  /**
   * The text value of the name.
   *
   * @return The text value of the name.
   */
  public String getValue() {
    return value;
  }

  /**
   * The text value of the name.
   *
   * @param value The text value of the name.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
