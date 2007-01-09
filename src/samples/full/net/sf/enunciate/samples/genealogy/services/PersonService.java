package net.sf.enunciate.samples.genealogy.services;

import net.sf.enunciate.samples.genealogy.data.Person;
import net.sf.enunciate.rest.annotations.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.sf.net/samples/full"
)
@RESTEndpoint
public interface PersonService {

  @SOAPBinding (
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  @Verb (
    VerbType.create
  )
  @Noun (
    "person"
  )
  Person storePerson(@NounValue Person person);

  Collection<Person> readPersons(Collection<String> personIds) throws ServiceException;

  @Verb(
    VerbType.delete
  )
  @Noun (
    "person"
  )
  void deletePerson(@ProperNoun String personId) throws ServiceException;
}
