package net.sf.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import net.sf.enunciate.samples.genealogy.client.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.client.cite.Source;
import net.sf.enunciate.samples.genealogy.client.cite.SourceXFireType;
import net.sf.enunciate.samples.genealogy.client.data.Event;
import net.sf.enunciate.samples.genealogy.client.data.Person;
import net.sf.enunciate.samples.genealogy.client.data.PersonXFireType;
import net.sf.enunciate.samples.genealogy.client.services.*;
import net.sf.enunciate.samples.genealogy.client.services.impl.PersonServiceImpl;
import net.sf.enunciate.samples.genealogy.client.services.impl.SourceServiceImpl;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.MessageExchange;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeMapping;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A very big test of the functionality of the full API deployed with the XFire client and server modules.
 * Since this test depends on the generated client API, it is assumed that the full API has already been
 * enunciated.  A system property named "enunciated.full.war" pointing to the war file created by the
 * process must also be provided.
 *
 * @author Ryan Heaton
 */
public class TestFullAPI extends TestCase {

  public static final String FULL_NAMESPACE = "http://enunciate.sf.net/samples/full";
  public static final String DATA_NAMESPACE = "http://enunciate.sf.net/samples/genealogy/data";
  public static final String CITE_NAMESPACE = "http://enunciate.sf.net/samples/genealogy/cite";

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

    SourceService sourceService = new SourceServiceImpl("http://localhost:" + port + "/" + context + "/soap/source-service");
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

    PersonService personService = new PersonServiceImpl("http://localhost:" + port + "/" + context + "/soap/PersonServiceService");
    ArrayList<String> ids = new ArrayList<String>(Arrays.asList("id1", "id2", "id3", "id4"));
    Collection persons = personService.readPersons(ids);
    for (Object o : persons) {
      Person person = (Person) o;
      assertTrue(ids.remove(person.getId()));
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
    assertEquals(HttpServletResponse.SC_OK, connection.getResponseCode());

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
    assertEquals(HttpServletResponse.SC_OK, connection.getResponseCode());
    assertTrue("expected empty data returned", connection.getInputStream().read() < 0);

    connection = (HttpURLConnection) new URL(String.format(sourceConnectString, "throw")).openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    assertEquals(500, connection.getResponseCode());
    assertEquals("some message", connection.getResponseMessage());

    connection = (HttpURLConnection) new URL(String.format(sourceConnectString, "valid")).openConnection();
    connection.setRequestMethod("DELETE");
    connection.connect();
    assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, connection.getResponseCode());

    String personConnectString = String.format("http://localhost:%s/%s/rest/person", port, context);
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
    ElementWriter writer = new ElementWriter(out, "person", "http://enunciate.sf.net/samples/genealogy/data");
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
    writer = new ElementWriter(out, "person", "http://enunciate.sf.net/samples/genealogy/data");
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
  }

}
