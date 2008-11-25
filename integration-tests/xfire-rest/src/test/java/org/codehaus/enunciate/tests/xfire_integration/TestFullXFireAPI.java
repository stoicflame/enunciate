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
import org.codehaus.enunciate.samples.genealogy.client.cite.InfoSet;
import org.codehaus.enunciate.samples.genealogy.client.cite.Note;
import org.codehaus.enunciate.samples.genealogy.client.cite.Source;
import org.codehaus.enunciate.samples.genealogy.client.cite.SourceXFireType;
import org.codehaus.enunciate.samples.genealogy.client.data.Event;
import org.codehaus.enunciate.samples.genealogy.client.data.Person;
import org.codehaus.enunciate.samples.genealogy.client.data.PersonXFireType;
import org.codehaus.enunciate.samples.genealogy.client.data.Relationship;
import org.codehaus.enunciate.samples.genealogy.client.exceptions.OutsideException;
import org.codehaus.enunciate.samples.genealogy.client.services.*;
import org.codehaus.enunciate.samples.genealogy.client.services.impl.PersonServiceImpl;
import org.codehaus.enunciate.samples.genealogy.client.services.impl.RelationshipServiceImpl;
import org.codehaus.enunciate.samples.genealogy.client.services.impl.SourceServiceImpl;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.exchange.MessageExchange;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.soap.SoapConstants;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.UploadFileSpec;
import com.meterware.httpunit.WebResponse;

/**
 * A very big test of the functionality of the full API deployed with the XFire client and server modules.
 * Since this test depends on the generated client API, it is assumed that the full API has already been
 * enunciated.  A system property named "enunciated.full.war" pointing to the war file created by the
 * process must also be provided.
 *
 * @author Ryan Heaton
 */
public class TestFullXFireAPI extends TestCase {

  public static final String FULL_NAMESPACE = "http://enunciate.codehaus.org/samples/full";
  public static final String DATA_NAMESPACE = "http://enunciate.codehaus.org/samples/genealogy/data";
  public static final String CITE_NAMESPACE = "http://enunciate.codehaus.org/samples/genealogy/cite";

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

    SourceService sourceService = new SourceServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/sources/source");
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

    long begin = System.currentTimeMillis();
    sourceService.addSource(new Source());
    sourceService.addSource(new Source());
    sourceService.addSource(new Source());
    long end = System.currentTimeMillis();
    long elapsed = (end - begin);
    assertTrue("Since this is a one-way operation, we expected the operations to take less than 15 seconds, even though the time it takes on the server to add a source is > 30 seconds per operation.  Took " + elapsed + " ms.", elapsed < 15000);

    assertEquals("newid", sourceService.addInfoSet("somesource", new InfoSet()));
    assertEquals("okay", sourceService.addInfoSet("othersource", new InfoSet()));
    assertEquals("intercepted", sourceService.addInfoSet("SPECIAL", new InfoSet()));
    assertEquals("intercepted2", sourceService.addInfoSet("SPECIAL2", new InfoSet()));
    assertEquals("resourceId", sourceService.addInfoSet("resource", new InfoSet()));
    try {
      sourceService.addInfoSet("unknown", new InfoSet());
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("unknown source id", e.getMessage());
      assertEquals("anyhow", e.getAnotherMessage());
    }

    //test SOAP headers.
    Event event1 = new Event();
    Event event2 = new Event();
    Event event3 = new Event();
    assertEquals("good", sourceService.addEvents("infoSetId", new Event[]{event1, event2, event3}, "good"));

    try {
      sourceService.addEvents("infoSetId", new Event[]{event1, event2, event3}, "illegal");
      fail("should have required a valid contributor id.");
    }
    catch (ServiceException e) {
      //fall through...
    }

    PersonService personService = new PersonServiceImpl("http://localhost:" + port + "/" + context + "/soap-services/PersonServiceService");
    ArrayList<String> ids = new ArrayList<String>(Arrays.asList("id1", "id2", "id3", "id4"));
    Collection persons = personService.readPersons(ids);
    for (Object o : persons) {
      Person person = (Person) o;
      assertTrue(ids.remove(person.getId()));
      assertEquals(new Date(1L), ((Event) person.getEvents().iterator().next()).getDate());

      Map notes = person.getNotes();
      assertEquals("text1", ((Note) notes.get("contributor1")).getText());
      assertEquals("text2", ((Note) notes.get("contributor2")).getText());
    }

    assertNull(personService.readPersons(null));

    personService.deletePerson("somebody");
    try {
      personService.deletePerson(null);
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("a person id must be supplied", e.getMessage());
      assertEquals("no person id.", e.getAnotherMessage());
    }
    try {
      personService.deletePerson("SPECIAL");
      fail("should have thrown an exception.");
    }
    catch (Exception e) {
      assertTrue(e.getMessage().contains("SPECIAL"));
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

    assertTrue(Arrays.equals(pixBytes, bytesOut.toByteArray()));

    //now disable MTOM and make sure it works
    ((PersonServiceImpl) personService).setMTOMEnabled(false);
    returnedPix = personService.storePerson(person).getPicture();
    bytesOut = new ByteArrayOutputStream();
    inputStream = returnedPix.getInputStream();
    byteIn = inputStream.read();
    while (byteIn > -1) {
      bytesOut.write(byteIn);
      byteIn = inputStream.read();
    }

    assertTrue(Arrays.equals(pixBytes, bytesOut.toByteArray()));

// todo: uncomment when wanting to spend time investigating why jaxb doesn't work with the JAX-WS types the same way it does its own.
//    Map map = personService.readFamily("myChildId");
//    for (Object key : map.keySet()) {
//      RelationshipType type = (RelationshipType) key;
//      if (type == RelationshipType.parent) {
//        Person parent = (Person) map.get(type);
//        assertEquals("parentId", parent.getId());
//      }
//      else if (type == RelationshipType.spouse) {
//        Person spouse = (Person) map.get(type);
//        assertEquals("spouseId", spouse.getId());
//      }
//      else if (type == RelationshipType.child) {
//        Person me = (Person) map.get(type);
//        assertEquals("myChildId", me.getId());
//      }
//    }
//
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

    try {
      relationshipService.getRelationships("outthrow");
      fail("Should have thrown the outside exception.");
    }
    catch (OutsideException e) {
      assertEquals("outside message", e.getMessage());
    }

    relationshipService.touch();

    //todo: test attachments as service parameters.
    //todo: test IN/OUT and OUT parameters when the xfire-client module supports them.
  }

  /**
   * tests the REST API.
   */
  public void testFullRESTAPI() throws Exception {
    int port = 8080;
    if (System.getProperty("container.port") != null) {
      port = Integer.parseInt(System.getProperty("container.port"));
    }

    String context = "full";
    if (System.getProperty("container.test.context") != null) {
      context = System.getProperty("container.test.context");
    }

    String sourceConnectString = String.format("http://localhost:%s/%s/rest/source/%%s", port, context);
    URL url = new URL(String.format(sourceConnectString, "valid"));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    assertEquals(200, connection.getResponseCode());

    SourceXFireType sourceType = new SourceXFireType();
    TypeMapping defaultTypeMapping = new DefaultTypeMappingRegistry(true).getDefaultTypeMapping();
    sourceType.setTypeMapping(defaultTypeMapping);
    Source source = (Source) sourceType.readObject(new ElementReader(connection.getInputStream()), new MessageContext());
    assertEquals("valid", source.getId());
    assertEquals(URI.create("uri:some-uri"), source.getLink());
    assertEquals("some-title", source.getTitle());
    connection.disconnect();

    connection = (HttpURLConnection) new URL(String.format(sourceConnectString, "invalid")).openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    assertEquals(200, connection.getResponseCode());
    assertTrue("expected empty data returned", connection.getInputStream().read() < 0);

    connection = (HttpURLConnection) new URL(String.format(sourceConnectString, "throw")).openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    assertEquals(500, connection.getResponseCode());
    String message = connection.getResponseMessage();
    assertTrue(message.startsWith("some") && message.endsWith("message")); //jetty replaces spaces with a "_"...

    connection = (HttpURLConnection) new URL(String.format(sourceConnectString, "valid")).openConnection();
    connection.setRequestMethod("DELETE");
    connection.connect();
    assertFalse(200 == connection.getResponseCode());

    String personConnectString = String.format("http://localhost:%s/%s/rest/pedigree/person", port, context);
    url = new URL(personConnectString);
    connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("PUT");
    connection.connect();

    PersonXFireType personType = new PersonXFireType();
    personType.setTypeMapping(defaultTypeMapping);
    Person person = new Person();
    person.setId("new-person");

    MessageContext messageContext = new MessageContext();
    messageContext.setProperty(SoapConstants.MTOM_ENABLED, String.valueOf(false));
    MessageExchange exchange = new MessageExchange(messageContext);
    exchange.setOutMessage(new OutMessage("urn:hi"));
    OutputStream out = connection.getOutputStream();
    ElementWriter writer = new ElementWriter(out, "person", "http://enunciate.codehaus.org/samples/genealogy/data");
    personType.writeObject(person, writer, messageContext);
    writer.close();
    writer.getXMLStreamWriter().close();
    out.flush();
    out.close();

    Person resultPerson = (Person) personType.readObject(new ElementReader(connection.getInputStream()), messageContext);
    assertEquals("new-person", resultPerson.getId());

    byte[] pixBytes = "this is a bunch of bytes that I would like to make sure are serialized correctly so that I can prove out that attachments are working properly".getBytes();
    person.setPicture(new DataHandler(new ByteArrayDataSource(pixBytes, "image/jpeg")));

    connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("PUT");
    connection.connect();
    out = connection.getOutputStream();
    writer = new ElementWriter(out, "person", "http://enunciate.codehaus.org/samples/genealogy/data");
    personType.writeObject(person, writer, messageContext);
    writer.close();
    writer.getXMLStreamWriter().close();
    out.flush();
    out.close();

    resultPerson = (Person) personType.readObject(new ElementReader(connection.getInputStream()), messageContext);
    DataHandler returnedPix = resultPerson.getPicture();
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    InputStream inputStream = returnedPix.getInputStream();
    int byteIn = inputStream.read();
    while (byteIn > -1) {
      bytesOut.write(byteIn);
      byteIn = inputStream.read();
    }

    assertTrue(Arrays.equals(pixBytes, bytesOut.toByteArray()));

    String fileConnectString = String.format("http://localhost:%s/%s/rest/pedigree/file", port, context);
    WebConversation wc = new WebConversation();
    PostMethodWebRequest post = new PostMethodWebRequest(fileConnectString);
    byte[] bytes1 = "this is some text for file 1".getBytes("utf-8");
    UploadFileSpec upload1 = new UploadFileSpec("file1.txt", new ByteArrayInputStream(bytes1), "text/plain");
    byte[] bytes2 = "this is some text for file 2".getBytes("utf-8");
    UploadFileSpec upload2 = new UploadFileSpec("file2.txt", new ByteArrayInputStream(bytes2), "text/plain");
    byte[] bytes3 = "this is some text for file 3".getBytes("utf-8");
    UploadFileSpec upload3 = new UploadFileSpec("file3.txt", new ByteArrayInputStream(bytes3), "text/plain");
    post.setMimeEncoded(true);
    post.setParameter("up1", new UploadFileSpec[] { upload1 });
    post.setParameter("up2", new UploadFileSpec[] { upload2 });
    post.setParameter("up3", new UploadFileSpec[] { upload3 });
    post.setParameter("length", "3;" + bytes1.length + ";" + bytes2.length + ";" + bytes3.length);
    WebResponse response = wc.getResponse(post);
    assertEquals(200, response.getResponseCode());

    personConnectString = String.format("http://localhost:%s/%s/rest/remover/pedigree/person", port, context);
    url = new URL(personConnectString + "/SPECIAL");
    connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("DELETE");
    connection.connect();
    assertEquals(500, connection.getResponseCode());
    assertTrue(connection.getResponseMessage().contains("SPECIAL"));
  }

}
