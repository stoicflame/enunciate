package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * An event assertion.
 *
 * @author Ryan Heaton
 */
public class Event extends OccurringAssertion {

  private EventType type;
  private String description;

  /**
   * The type of this event.
   *
   * @return The type of this event.
   */
  @XmlAttribute
  public EventType getType() {
    return type;
  }

  /**
   * The type of this event.
   *
   * @param type The type of this event.
   */
  public void setType(EventType type) {
    this.type = type;
  }

  /**
   * A description of this event.
   *
   * @return A description of this event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A description of this event.
   *
   * @param description A description of this event.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
