package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A generic fact assertion.
 *
 * @author Ryan Heaton
 */
public class Fact extends OccurringAssertion {

  private FactType type;
  private String value;
  private String description;

  /**
   * The fact type.
   *
   * @return The fact type.
   */
  @XmlAttribute
  public FactType getType() {
    return type;
  }

  /**
   * The fact type.
   *
   * @param type The fact type.
   */
  public void setType(FactType type) {
    this.type = type;
  }

  /**
   * The value of the fact.
   *
   * @return The value of the fact.
   */
  public String getValue() {
    return value;
  }

  /**
   * The value of the fact.
   *
   * @param value The value of the fact.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * A description of the fact.
   *
   * @return A description of the fact.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A description of the fact.
   *
   * @param description A description of the fact.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
