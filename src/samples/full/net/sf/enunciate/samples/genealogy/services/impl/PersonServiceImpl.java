package net.sf.enunciate.samples.genealogy.services.impl;

import net.sf.enunciate.samples.genealogy.data.Person;
import net.sf.enunciate.samples.genealogy.services.PersonService;

import javax.jws.WebService;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.sf.enunciate.samples.genealogy.services.PersonService"
)
public class PersonServiceImpl implements PersonService {

  public Person storePerson(Person person) {
    return null;
  }

  public Collection<Person> readPersons(Collection<String> personIds) {
    return null;
  }

  public void deletePerson(String personId) {
  }
}
