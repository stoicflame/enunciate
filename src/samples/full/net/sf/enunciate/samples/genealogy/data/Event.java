package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
public class Event extends OccurringAssertion {

  private EventType type;
  private String description;

  @XmlAttribute
  public EventType getType() {
    return type;
  }

  public void setType(EventType type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
