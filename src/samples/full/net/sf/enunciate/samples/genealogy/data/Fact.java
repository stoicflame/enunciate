package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
public class Fact extends OccurringAssertion {

  private FactType type;
  private String value;
  private String description;

  @XmlAttribute
  public FactType getType() {
    return type;
  }

  public void setType(FactType type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
