/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.genealogy.services;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.enunciate.rest.annotations.NounValue;
import org.codehaus.enunciate.samples.genealogy.data.Person;
import org.codehaus.enunciate.samples.genealogy.data.RootElementMap;
import org.codehaus.enunciate.samples.genealogy.data.RootElementMapWrapper;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.*;
import javax.xml.ws.soap.MTOM;
import java.util.Collection;

/**
 * The person service is used to access persons in the database.
 * 
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.codehaus.org/samples/full"
)
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
  @PUT
  @Path ("/pedigree/person")
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
  @DELETE
  @Path("/remover/pedigree/person/{id}")
  void deletePerson(@PathParam ("id") String personId) throws ServiceException;

  /**
   * Store some generic properties.
   *
   * @param map The map of generic properties.
   * @return The generic properties.
   * @throws ServiceException Upon a problem.
   */
  @PUT
  @Path("/properties/generic")
  RootElementMapWrapper storeGenericProperties(RootElementMapWrapper map) throws ServiceException;

  /**
   * Uploads some files.
   *
   * @param files The files
   * @param length The length(s) of the files.
   * @param somename Another name.
   */
  @WebMethod ( exclude = true )
  @POST
  @Path("/posterdude")
  @ResourceMethodSignature(
    input = Person.class,
    output = Person.class,
    queryParams = { @QueryParam("length"), @QueryParam("somename") }
  )
  void uploadFiles(DataHandler[] files, String length) throws ServiceException;
// todo: uncomment when wanting to spend time investigating why jaxb doesn't work with the JAX-WS types the same way it does its own.
//  /**
//   * Reads the family of a given person.  Tests out maps.
//   *
//   * @param personId The id of the person for which to read the family.
//   * @return The persons in the family by relationship type.
//   * @throws ServiceException If some problem occurred.
//   */
//  Map<RelationshipType, Person> readFamily(String personId) throws ServiceException;
}
