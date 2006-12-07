package net.sf.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import net.sf.enunciate.samples.genealogy.client.cite.Source;
import net.sf.enunciate.samples.genealogy.client.services.SourceService;
import net.sf.enunciate.samples.genealogy.client.services.impl.SourceServiceImpl;

import java.net.URI;

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
    SourceService sourceService = new SourceServiceImpl("localhost", 7373, "/full/soap/source-service");
    Source source = sourceService.getSource("valid");
    assertEquals("valid", source.getId());
    assertEquals(URI.create("uri:some-uri"), source.getLink());
    assertEquals("some-title", source.getTitle());
    assertNull(sourceService.getSource("invalid"));

  }

}
