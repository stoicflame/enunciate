package net.java.ws.addressbook.services;

import net.java.ws.addressbook.domain.ContactList;
import net.java.ws.addressbook.domain.Contact;
import net.java.ws.addressbook.domain.ContactType;

import javax.jws.WebService;

import org.codehaus.enunciate.rest.annotations.*;

/**
 * An address book that provides access to a set of contacts.
 *
 * @author Ryan Heaton
 */
@WebService
@RESTEndpoint
public interface AddressBook {

  /**
   * Get a contact by id.
   *
   * @param id The id of the contact.
   * @return The contact.
   * @throws AddressBookException If the contact wasn't found.
   */
  @Noun (
    "contact"
  )
  @Verb (
    VerbType.read
  )
  Contact getContact(@ProperNoun Integer id) throws AddressBookException;

  /**
   * Find contacts by name.
   *
   * @param name The name to search for.
   * @return The contacts that were found.
   */
  ContactList findContactsByName(String name) throws AddressBookException;

  /**
   * Find contats by type.
   *
   * @param type The type of contact.
   * @return The contacts.
   */
  ContactList findContactsByType(ContactType type) throws AddressBookException;
}
