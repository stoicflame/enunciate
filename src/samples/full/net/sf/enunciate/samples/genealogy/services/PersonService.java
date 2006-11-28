package net.sf.enunciate.samples.genealogy.services;

import net.sf.enunciate.samples.genealogy.data.Person;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.sf.net/samples/full"
)
public interface PersonService {

  @SOAPBinding (
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  Person storePerson(Person person);

  Collection<Person> readPersons(Collection<String> personIds) throws ServiceException;

  void deletePerson(String personId) throws ServiceException;
}
