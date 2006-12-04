package net.sf.enunciate.tests.xfire_integration;

import junit.framework.TestCase;
import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import java.io.File;

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
    Server server = new Server();
    server.addListener(new InetAddrPort(7373));
    server.addWebApplication("full", new File("my-war").toURL().toString());
  }

}
