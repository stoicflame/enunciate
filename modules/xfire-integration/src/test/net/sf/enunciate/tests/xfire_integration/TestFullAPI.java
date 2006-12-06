package net.sf.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import java.io.File;
import java.net.URI;

import net.sf.enunciate.samples.genealogy.client.services.impl.PersonServiceImpl;
import net.sf.enunciate.samples.genealogy.client.services.impl.SourceServiceImpl;
import net.sf.enunciate.samples.genealogy.client.services.PersonService;
import net.sf.enunciate.samples.genealogy.client.services.SourceService;
import net.sf.enunciate.samples.genealogy.client.cite.Source;

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
//    String warPath = System.getProperty("full.war.path");
//    assertNotNull("A path to the created war file must be present in the 'full.war.path' property.");
//    File warFile = new File(warPath);
//    assertTrue("Non-existant file: " + warFile, warFile.exists());
//
//    Server server = new Server();
//    server.addListener(new InetAddrPort(7373));
//    server.addWebApplication("full", warFile.toURL().toString());
//    server.start();

    SourceService sourceService = new SourceServiceImpl("localhost", 7373, "/full/soap/source-service");
    Source source = sourceService.getSource("valid");
    assertEquals("valid", source.getId());
    assertEquals(URI.create("uri:some-uri"), source.getLink());
    assertEquals("some-title", source.getTitle());
    assertNull(sourceService.getSource("invalid"));

//    server.stop();
  }

}
