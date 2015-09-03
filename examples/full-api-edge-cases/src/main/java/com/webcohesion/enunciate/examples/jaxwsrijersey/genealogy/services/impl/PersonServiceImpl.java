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

package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.impl;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.*;
import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.PersonService;
import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.ServiceException;
import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.joda.time.DateTime;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The person service is used to access persons in the database.
 *
 * @author Ryan Heaton
 * @deprecated
 */
@WebService (
  endpointInterface = "com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.PersonService"
)
@com.sun.xml.ws.developer.StreamingAttachment (
  parseEagerly = true,
  memoryThreshold = 40000L
)
@Path ("")
@StatusCodes({@ResponseCode ( code = 401, condition = "If authentication is needed.")})
@Facet (name = "custom")
public class PersonServiceImpl implements PersonService {

  public Person storePerson(Person person) {
    return person;
  }

  public PersonExt readExtPerson(String id) {
    return null;
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

      Event event = new Event();
      event.setDate(new DateTime(1L));
      person.setEvents(Arrays.asList(event));
    }

    return persons;
  }

  public void deletePerson(String PErsonId, String message) throws ServiceException {
    if (PErsonId == null) {
      throw new ServiceException("a person id must be supplied", "no person id.");
    }
  }

  public PersonExt readPersonAdmin(String id) {
    return new PersonExt();
  }

  public Map<RelationshipType, Person> readFamily(String personId) throws ServiceException {
    HashMap<RelationshipType, Person> pedigree = new HashMap<RelationshipType, Person>();
    Person person = new Person();
    person.setId("parent");
    pedigree.put(RelationshipType.parent, person);
    Person spouse = new Person();
    spouse.setId("spouse");
    pedigree.put(RelationshipType.spouse, spouse);
    Person child = new Person();
    child.setId(personId);
    pedigree.put(RelationshipType.child, child);
    return pedigree;
  }

  public RootElementMapWrapper storeGenericProperties(RootElementMapWrapper map) throws ServiceException {
    return map;
  }

  public void postMultipart( InputStream file1, InputStream file2 ) {
  }

  /**
   * The id of the OAuth 2 access token used for identification and authorization of the user (and agent) making the request.
   *
   * @param sessionId The id of the OAuth 2 access token used for identification and authorization of the user (and agent) making the request.
   */
  @QueryParam ("access_token")
  public void setSessionId(String sessionId) {
  }
}
