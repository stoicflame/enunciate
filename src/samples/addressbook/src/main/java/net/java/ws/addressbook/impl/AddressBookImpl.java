package net.java.ws.addressbook.impl;

import net.java.ws.addressbook.services.AddressBook;
import net.java.ws.addressbook.services.AddressBookException;
import net.java.ws.addressbook.domain.Contact;
import net.java.ws.addressbook.domain.ContactList;
import net.java.ws.addressbook.domain.ContactType;

import java.util.*;

import org.codehaus.enunciate.rest.annotations.RESTEndpoint;

import javax.jws.WebService;
import javax.ws.rs.Path;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.java.ws.addressbook.services.AddressBook"
)
@Path ("/contact")
public class AddressBookImpl implements AddressBook {

  private static final Map<Integer, Contact> STORE = loadContacts();

  public Contact getContact(Integer id) throws AddressBookException {
    Contact contact = STORE.get(id);
    if (contact == null) {
      throw new AddressBookException("contact not found: " + id);
    }
    return contact;
  }

  public Contact postContact(Contact contact) throws AddressBookException {
    STORE.put(contact.getId(), contact);
    return contact;
  }

  public ContactList findContactsByName(String name) throws AddressBookException {
    ArrayList<Contact> contacts = new ArrayList<Contact>();
    if (name != null) {
      for (Contact contact : STORE.values()) {
        if (contact.getName().toLowerCase().contains(name.toLowerCase())) {
          contacts.add(contact);
        }
      }
    }
    ContactList list = new ContactList();
    list.setContacts(contacts);
    return list;
  }

  public ContactList findContactsByType(ContactType type) throws AddressBookException {
    ArrayList<Contact> contacts = new ArrayList<Contact>();
    for (Contact contact : STORE.values()) {
      if (contact.getContactType().equals(type)) {
        contacts.add(contact);
      }
    }
    ContactList list = new ContactList();
    list.setContacts(contacts);
    return list;
  }

  private static Map<Integer, Contact> loadContacts() {
    final int size = 20;
    Random random = new Random();
    String[] firstNames = new String[]{"Sally", "George", "Harold", "Tammy", "Robert", "Daniel", "Jane", "Mike", "David", "John"};
    String[] lastNames = new String[]{"Beach", "Jobs", "Gates", "Bush", "Clinton", "Gore", "Moore", "Jones", "Adams", "Washington", "Smith"};
    String[] addresses = new String[]{"1 First Street", "2 Second Street", "3 Third Street"};
    String[] cities = new String[]{"Long Beach, CA", "New York, NY", "Orlando, FL", "Honolulu, HI", "Oklahoma City, OK"};
    String[] phoneNumbers = new String[]{"111-1111", "222-2222", "333-3333", "444-4444", "555-5555", "666-6666", "777-7777"};

    HashMap<Integer, Contact> contacts = new HashMap<Integer, Contact>();
    for (int i = 0; i < size; i++) {
      Contact contact = new Contact();
      contact.setId(i);
      contact.setName(firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)]);
      contact.setAddress1(addresses[random.nextInt(addresses.length)]);
      contact.setCity(cities[random.nextInt(cities.length)]);
      contact.setPhone(phoneNumbers[random.nextInt(phoneNumbers.length)]);
      contact.setContactType(ContactType.values()[random.nextInt(ContactType.values().length)]);
      contacts.put(i, contact);
    }

    return contacts;
  }
}
