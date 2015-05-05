package net.java.ws.addressbook.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * A contact in the address book.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Contact {

  private int id;
  private String name;
  private String phone;
  private String address1;
  private String address2;
  private String city;
  private ContactType contactType;
  private Date updated = new Date();

  /**
   * The id of the contact.
   *
   * @return The id of the contact.
   */
  public int getId() {
    return id;
  }

  /**
   * The id of the contact.
   *
   * @param id The id of the contact.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * The name of the contact.
   *
   * @return The name of the contact.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the contact.
   *
   * @param name The name of the contact.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The phone of the contact.
   *
   * @return The phone of the contact.
   */
  public String getPhone() {
    return phone;
  }

  /**
   * TThe phone of the contact.
   *
   * @param phone The phone of the contact.
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * The first address field of the contact.
   *
   * @return The first address field of the contact.
   */
  public String getAddress1() {
    return address1;
  }

  /**
   * The first address field of the contact.
   *
   * @param address1 The first address field of the contact.
   */
  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  /**
   * The second address field of the contact.
   *
   * @return The second address field of the contact.
   */
  public String getAddress2() {
    return address2;
  }

  /**
   * The second address field of the contact.
   *
   * @param address2 The second address field of the contact.
   */
  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  /**
   * The city of the contact.
   *
   * @return The city of the contact.
   */
  public String getCity() {
    return city;
  }

  /**
   * The city of the contact.
   *
   * @param city The city of the contact.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * The contact type.
   *
   * @return The contact type.
   */
  public ContactType getContactType() {
    return contactType;
  }

  /**
   * The contact type.
   *
   * @param contactType The contact type.
   */
  public void setContactType(ContactType contactType) {
    this.contactType = contactType;
  }

  /**
   * The updated date.
   *
   * @return The updated date.
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   * The updated date.
   *
   * @param updated The updated date.
   */
  public void setUpdated(Date updated) {
    this.updated = updated;
  }
}
