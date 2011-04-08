package org.codehaus.enunciate.samples.petclinic.schema;

import org.codehaus.enunciate.qname.XmlQNameEnumRef;

import javax.xml.namespace.QName;

/**
 * Simple JavaBean domain object representing an person.
 *
 * @author Ken Krebs
 */
public class Person extends Entity {

  private String firstName;
  private String lastName;
  private String address;
  private String city;
  private String telephone;
  private QName favoriteFood;
  private QName favoriteColor;
  private QName education;

  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getAddress() {
    return this.address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return this.city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getTelephone() {
    return this.telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  @XmlQNameEnumRef(FavoriteFood.class)
  public QName getFavoriteFood() {
    return favoriteFood;
  }

  public void setFavoriteFood(QName favoriteFood) {
    this.favoriteFood = favoriteFood;
  }

  @XmlQNameEnumRef(FavoriteColor.class)
  public QName getFavoriteColor() {
    return favoriteColor;
  }

  public void setFavoriteColor(QName favoriteColor) {
    this.favoriteColor = favoriteColor;
  }

  @XmlQNameEnumRef(Education.class)
  public QName getEducation() {
    return education;
  }

  public void setEducation(QName education) {
    this.education = education;
  }
}
