package net.java.ws.addressbook.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * A list of contacts.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class ContactList {

  private Collection<Contact> contacts;

  /**
   * The contact list.
   *
   * @return The contact list.
   */
  public Collection<Contact> getContacts() {
    return contacts;
  }

  /**
   * The contact list.
   *
   * @param contacts The contact list.
   */
  public void setContacts(Collection<Contact> contacts) {
    this.contacts = contacts;
  }
}
