package net.sf.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import net.sf.enunciate.samples.genealogy.client.cite.Source;
import net.sf.enunciate.samples.genealogy.client.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.client.services.SourceService;
import net.sf.enunciate.samples.genealogy.client.services.ServiceException;
import net.sf.enunciate.samples.genealogy.client.services.PersonService;
import net.sf.enunciate.samples.genealogy.client.services.impl.SourceServiceImpl;
import net.sf.enunciate.samples.genealogy.client.services.impl.PersonServiceImpl;
import net.sf.enunciate.samples.genealogy.client.data.Person;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;

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
  public void testFullAPI() throws Exception {
    if (System.getProperty("container.port") == null) {
      fail("A container.port property must be provided for this test.");
    }

    int port = Integer.parseInt(System.getProperty("container.port"));
    String context = "full";
    if (System.getProperty("container.test.context") != null) {
      context = System.getProperty("container.test.context");
    }

    SourceService sourceService = new SourceServiceImpl("localhost", port, "/" + context + "/soap/source-service");
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

    long begin = System.currentTimeMillis();
    sourceService.addSource(new Source());
    sourceService.addSource(new Source());
    sourceService.addSource(new Source());
    long end = System.currentTimeMillis();
    long elapsed = (end - begin);
    assertTrue("Since this is a one-way operation, we expected the operations to take less than 15 seconds, even though the time it takes on the server to add a source is > 30 seconds per operation.  Took " + elapsed + " ms.", elapsed < 15000);

    assertEquals("newid", sourceService.addInfoSet("somesource", new InfoSet()));
    assertEquals("okay", sourceService.addInfoSet("othersource", new InfoSet()));
    try {
      sourceService.addInfoSet("unknown", new InfoSet());
      fail("Should have thrown the exception.");
    }
    catch (ServiceException e) {
      assertEquals("unknown source id", e.getMessage());
      assertEquals("anyhow", e.getAnotherMessage());
    }

    PersonService personService = new PersonServiceImpl("localhost", port, "/" + context + "/soap/PersonServiceService");
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

    //todo: test SOAP headers.
    //todo: test attachments.
    //todo: test attachments as service parameters.
    //todo: test throwing an explicit web fault (as opposed to just an implicit one).

    //todo: test IN/OUT and OUT parameters when the xfire-client module supports them.
  }

}
