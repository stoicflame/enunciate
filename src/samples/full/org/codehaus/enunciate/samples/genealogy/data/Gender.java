package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A gender assertion.
 *
 * @author Ryan Heaton
 */
public class Gender extends Assertion {

  private GenderType type;

  /**
   * The type of gender.
   *
   * @return The type of gender.
   */
  @XmlAttribute
  public GenderType getType() {
    return type;
  }

  /**
   * The type of gender.
   *
   * @param type The type of gender.
   */
  public void setType(GenderType type) {
    this.type = type;
  }
}
