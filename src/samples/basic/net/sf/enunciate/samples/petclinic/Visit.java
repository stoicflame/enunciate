/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A visit of a pet.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://net.sf.enunciate/samples/petclinic/vets"
)
public class Visit extends Entity {

  private Date date;
  private String description;
  private Pet pet;

  /**
   * Creates a new instance of Visit for the current date
   */
  public Visit() {
    this.date = new Date();
  }

  /**
   * The date.
   *
   * @return The date.
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * The date.
   *
   * @param date The date.
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * The description of the visit.
   *
   * @return The description of the visit.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * The description of the visit.
   *
   * @param description The description of the visit.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The pet for the visit.
   *
   * @return The pet for the visit.
   */
  public Pet getPet() {
    return this.pet;
  }

  /**
   * The pet for the visit.
   *
   * @param pet The pet for the visit.
   */
  protected void setPet(Pet pet) {
    this.pet = pet;
  }

}
