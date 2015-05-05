package net.java.ws.addressbook.services;

import net.java.ws.addressbook.domain.ContactList;
import net.java.ws.addressbook.domain.Contact;
import net.java.ws.addressbook.domain.ContactType;

import javax.jws.WebService;
import javax.ws.rs.*;

/**
 * An address book that provides access to a set of contacts.
 *
 * @author Ryan Heaton
 */
@WebService
public interface AddressBook {

  /**
   * Get a contact by id.
   *
   * @param id The id of the contact.
   * @return The contact.
   * @throws AddressBookException If the contact wasn't found.
   */
  @GET
  @Path("/{id}")
  @Produces ( { "application/xml", "application/x-amf", "application/json" } )
  Contact getContact(@PathParam("id") Integer id) throws AddressBookException;

  /**
   * Post a new contact, or edit an existing one.
   *
   * @param contact the contact.
   * @return the contact that was posted.
   * @throws AddressBookException The address book exception.
   */
  @POST
  @Consumes ( { "application/xml", "application/x-amf" } )
  @Produces ( { "application/xml", "application/x-amf" } )
  Contact postContact(Contact contact) throws AddressBookException;

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
