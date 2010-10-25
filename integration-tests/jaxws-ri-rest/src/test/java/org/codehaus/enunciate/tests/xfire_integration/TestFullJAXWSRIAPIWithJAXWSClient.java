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

package org.codehaus.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.cite.InfoSet;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.cite.Source;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.data.*;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.services.*;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.services.impl.AssertionServiceImpl;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.services.impl.PersonServiceImpl;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.services.impl.RelationshipServiceImpl;
import org.codehaus.enunciate.samples.genealogy.jaxws_client.services.impl.SourceServiceImpl;


import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class TestFullJAXWSRIAPIWithJAXWSClient extends TestCase {

  /**
   * Tests the full API
   */
  public void testFullSOAPAPI() throws Exception {
    int port = 8080;
    if (System.getProperty("container.port") != null) {
      port = Integer.parseInt(System.getProperty("container.port"));
    }

    String context = "full";
    if (System.getProperty("container.test.context") != null) {
      context = System.getProperty("container.test.context");
    }

    SourceServiceImpl impl = new SourceServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/sources/source");
    SourceService sourceService = impl;
    Source source = sourceService.getSource("valid");
    assertEquals("valid", source.getId());
    assertEquals(URI.create("uri:some-uri"), source.getLink());
    assertEquals("some-title", source.getTitle());
    assertNull(sourceService.getSource("invalid"));

    try {
      sourceService.getSource("throw");
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("some message", e.getMessage());
      assertEquals("another message", e.getAnotherMessage());
    }

    try {
      sourceService.getSource("unknown");
      fail("should have thrown the unknown source exception.");
    }
    catch (UnknownSourceException e) {
      UnknownSourceBean bean = e.getFaultInfo();
      assertEquals("unknown", bean.getSourceId());
      assertEquals(888, bean.getErrorCode());
    }

    assertEquals("newid", sourceService.addInfoSet("somesource", new InfoSet()));
    assertEquals("okay", sourceService.addInfoSet("othersource", new InfoSet()));
    assertEquals("resourceId", sourceService.addInfoSet("resource", new InfoSet()));
    try {
      sourceService.addInfoSet("unknown", new InfoSet());
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("unknown source id", e.getMessage());
      assertEquals("anyhow", e.getAnotherMessage());
    }

    PersonService personService = new PersonServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/PersonServiceService");
    ArrayList<String> ids = new ArrayList<String>(Arrays.asList("id1", "id2", "id3", "id4"));
    Collection persons = personService.readPersons(ids);
    for (Object o : persons) {
      Person person = (Person) o;
      assertTrue(ids.remove(person.getId()));
      assertEquals(new Date(1L), ((Event) person.getEvents().iterator().next()).getDate());
    }

    Collection<Person> empty = personService.readPersons(null);
    assertTrue(empty == null || empty.isEmpty());

    personService.deletePerson("somebody");
    try {
      personService.deletePerson(null);
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("a person id must be supplied", e.getMessage());
      assertEquals("no person id.", e.getAnotherMessage());
    }

    Person person = new Person();
    person.setId("new-person");
    assertEquals("new-person", personService.storePerson(person).getId());

    byte[] pixBytes = "this is a bunch of bytes that I would like to make sure are serialized correctly so that I can prove out that attachments are working properly".getBytes();
    person.setPicture(new DataHandler(new ByteArrayDataSource(pixBytes, "image/jpeg")));

    DataHandler returnedPix = personService.storePerson(person).getPicture();
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    InputStream inputStream = returnedPix.getInputStream();
    int byteIn = inputStream.read();
    while (byteIn > -1) {
      bytesOut.write(byteIn);
      byteIn = inputStream.read();
    }

    RootElementMapAdapted map = new RootElementMapAdapted();
    ArrayList<RootElementMapAdaptedEntry> entries = new ArrayList<RootElementMapAdaptedEntry>();
    RootElementMapAdaptedEntry entry1 = new RootElementMapAdaptedEntry();
    entry1.setKey("person1");
    RootElementMapAdaptedValue value1 = new RootElementMapAdaptedValue();
    Person person1 = new Person();
    person1.setId("person1id");
    value1.setValue(Arrays.asList((Object) person1));
    entry1.setValue(value1);
    entries.add(entry1);

    RootElementMapAdaptedEntry entry2 = new RootElementMapAdaptedEntry();
    entry2.setKey("person2");
    RootElementMapAdaptedValue value2 = new RootElementMapAdaptedValue();
    Person person2 = new Person();
    person2.setId("person2id");
    value2.setValue(Arrays.asList((Object) person2));
    entry2.setValue(value2);
    entries.add(entry2);

    RootElementMapAdaptedEntry entry3 = new RootElementMapAdaptedEntry();
    entry3.setKey("source1");
    RootElementMapAdaptedValue value3 = new RootElementMapAdaptedValue();
    Source source1 = new Source();
    source1.setId("source1id");
    value3.setValue(Arrays.asList((Object) source1));
    entry3.setValue(value3);
    entries.add(entry3);

    RootElementMapAdaptedEntry entry4 = new RootElementMapAdaptedEntry();
    entry4.setKey("source2");
    RootElementMapAdaptedValue value4 = new RootElementMapAdaptedValue();
    Source source2 = new Source();
    source2.setId("source2id");
    value4.setValue(Arrays.asList((Object) source2));
    entry4.setValue(value4);
    entries.add(entry4);
    map.setEntry(entries);
    RootElementMapWrapper wrapper = new RootElementMapWrapper();
    wrapper.setMap(map);
    wrapper = personService.storeGenericProperties(wrapper);
    RootElementMapAdapted retVal = wrapper.getMap();
    assertNotNull(retVal.getEntry());
    assertEquals(4, retVal.getEntry().size());
//    assertTrue(retVal.getEntry().get(0).getValue().getValue() instanceof Person);
//    assertTrue(retVal.getEntry().get(1).getValue().getValue() instanceof Person);
//    assertTrue(retVal.getEntry().get(2).getValue().getValue() instanceof Source);
//    assertTrue(retVal.getEntry().get(3).getValue().getValue() instanceof Source);

    RelationshipService relationshipService = new RelationshipServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/RelationshipServiceService");
    List list = relationshipService.getRelationships("someid");
    for (int i = 0; i < list.size(); i++) {
      Relationship relationship = (Relationship) list.get(i);
      assertEquals(String.valueOf(i), relationship.getId());
    }

    try {
      relationshipService.getRelationships("throw");
      fail("Should have thrown the relationship service exception, even though it wasn't annotated with @WebFault.");
    }
    catch (RelationshipException e) {
      assertEquals("hi", e.getMessage());
    }

    relationshipService.touch();

    AssertionService assertionService = new AssertionServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/AssertionServiceService");
    List<Assertion> assertions = assertionService.readAssertions();
    Assertion gender = assertions.get(0);
    assertEquals("gender", gender.getId());
    assertTrue(gender instanceof Gender);
    Assertion name = assertions.get(1);
    assertEquals("name", name.getId());
    assertTrue(name instanceof Name);
  }

}