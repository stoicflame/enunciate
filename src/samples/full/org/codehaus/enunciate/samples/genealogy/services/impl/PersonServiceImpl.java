package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.data.Person;
import org.codehaus.enunciate.samples.genealogy.services.PersonService;
import org.codehaus.enunciate.samples.genealogy.services.ServiceException;
import org.codehaus.enunciate.rest.annotations.RESTEndpoint;

import javax.jws.WebService;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.genealogy.services.PersonService"
)
@RESTEndpoint
public class PersonServiceImpl implements PersonService {

  public Person storePerson(Person person) {
    return person;
  }

  public Collection<Person> readPersons(Collection<String> personIds) {
    if (personIds == null) {
      return null;
    }

    ArrayList<Person> persons = new ArrayList<Person>(personIds.size());
    for (String personId : personIds) {
      Person person = new Person();
      person.setId(personId);
      persons.add(person);
    }

    return persons;
  }

  public void deletePerson(String personId) throws ServiceException {
    if (personId == null) {
      throw new ServiceException("a person id must be supplied", "no person id.");
    }
  }
}
