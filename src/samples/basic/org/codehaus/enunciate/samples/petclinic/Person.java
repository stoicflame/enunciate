/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package org.codehaus.enunciate.samples.petclinic;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A person.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://org.codehaus.enunciate/samples/petclinic/owners"
)
public class Person extends Entity {

  private String firstName;
  private String lastName;
  private String address;
  private String city;
  private String telephone;

  /**
   * The persons first name.
   *
   * @return The persons first name.
   */
  public String getFirstName() {
    return this.firstName;
  }

  /**
   * The persons first name.
   *
   * @param firstName The persons first name.
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * The persons last name.
   *
   * @return The persons last name.
   */
  public String getLastName() {
    return this.lastName;
  }

  /**
   * The persons last name.
   *
   * @param lastName The persons last name.
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * The address of the person.
   *
   * @return The address of the person.
   */
  public String getAddress() {
    return this.address;
  }

  /**
   * The address of the person.
   *
   * @param address The address of the person.
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * The city.
   *
   * @return The city.
   */
  public String getCity() {
    return this.city;
  }

  /**
   * The city.
   *
   * @param city The city.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * The telephone.
   *
   * @return The telephone.
   */
  public String getTelephone() {
    return this.telephone;
  }

  /**
   * The telephone.
   *
   * @param telephone The telephone.
   */
  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

}
