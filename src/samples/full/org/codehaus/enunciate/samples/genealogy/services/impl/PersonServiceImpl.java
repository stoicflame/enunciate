/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.data.Person;
import org.codehaus.enunciate.samples.genealogy.data.Event;
import org.codehaus.enunciate.samples.genealogy.services.PersonService;
import org.codehaus.enunciate.samples.genealogy.services.ServiceException;
import org.codehaus.enunciate.rest.annotations.RESTEndpoint;
import org.joda.time.DateTime;

import javax.jws.WebService;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

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

      Event event = new Event();
      event.setDate(new DateTime(1L));
      person.setEvents(Arrays.asList(event));
    }

    return persons;
  }

  public void deletePerson(String personId) throws ServiceException {
    if (personId == null) {
      throw new ServiceException("a person id must be supplied", "no person id.");
    }
  }
}
