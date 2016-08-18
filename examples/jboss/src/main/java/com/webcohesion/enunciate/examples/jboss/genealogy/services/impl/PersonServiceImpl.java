/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.examples.jboss.genealogy.services.impl;

import com.webcohesion.enunciate.examples.jboss.genealogy.data.Event;
import com.webcohesion.enunciate.examples.jboss.genealogy.data.Person;
import com.webcohesion.enunciate.examples.jboss.genealogy.data.RelationshipType;
import com.webcohesion.enunciate.examples.jboss.genealogy.services.PersonService;
import com.webcohesion.enunciate.examples.jboss.genealogy.services.ServiceException;
import org.jboss.resteasy.annotations.Form;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.xml.ws.soap.MTOM;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "com.webcohesion.enunciate.examples.jboss.genealogy.services.PersonService"
)
@Path ("")
@MTOM
public class PersonServiceImpl implements PersonService {

  @PUT
  @Path ("/pedigree/person")
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

      Event event = new Event();
      event.setDate(new Date(1L));
      person.setEvents(Arrays.asList(event));
    }

    return persons;
  }

  @DELETE
  @Path("/remover/pedigree/person/{id}")
  public void deletePerson(@PathParam ("id") String personId) throws ServiceException {
    if (personId == null) {
      throw new ServiceException("a person id must be supplied", "no person id.");
    }
  }

  @POST
  @Path("/pedigree/person/form")
  public void submitPerson(@Form PersonForm personForm) {

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
}
