package org.codehaus.enunciate.samples.genealogy.services;

import org.codehaus.enunciate.samples.genealogy.data.Person;
import org.codehaus.enunciate.rest.annotations.*;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Collection;

/**
 * The person service is used to access persons in the database.
 * 
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.codehaus.org/samples/full"
)
@RESTEndpoint
public interface PersonService {

  /**
   * Stores a person in the database.
   *
   * @param person The person to store in the database.
   * @return The person that was stored (presumably modified for storage).
   */
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

  /**
   * Reads a set of persons from the database.  Intended as an example of
   * collections as SOAP parameters.
   * @param personIds The ids of the persons to read.
   * @return The persons that were read.
   * @throws ServiceException If the read of one or more of the people failed.
   */
  Collection<Person> readPersons(Collection<String> personIds) throws ServiceException;

  /**
   * Deletes a person from the database.  Not a one-way method, but still void.
   *
   * @param personId The id of the person.
   * @throws ServiceException If some problem occurred when deleting the person.
   */
  @Verb(
    VerbType.delete
  )
  @Noun (
    "person"
  )
  void deletePerson(@ProperNoun String personId) throws ServiceException;
}
