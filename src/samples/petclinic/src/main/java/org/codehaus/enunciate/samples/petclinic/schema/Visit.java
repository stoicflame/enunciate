package org.codehaus.enunciate.samples.petclinic.schema;

import java.util.Date;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 */
public class Visit extends Entity {

  private Date date;
  private String description;
  private Integer petId;

  /**
   * Creates a new instance of Visit for the current date
   */
  public Visit() {
    this.date = new Date();
  }

  /**
   * Getter for property date.
   *
   * @return Value of property date.
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * Setter for property date.
   *
   * @param date New value of property date.
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Getter for property description.
   *
   * @return Value of property description.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Setter for property description.
   *
   * @param description New value of property description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPetId() {
    return petId;
  }

  public void setPetId(Integer petId) {
    this.petId = petId;
  }

}
