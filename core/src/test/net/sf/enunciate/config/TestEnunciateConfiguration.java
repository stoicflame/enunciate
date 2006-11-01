package net.sf.enunciate.config;

import junit.framework.TestCase;
import net.sf.enunciate.modules.DeploymentModule;
import org.xml.sax.SAXException;

import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateConfiguration extends TestCase {

  /**
   * Tests loading the configuration from a stream.
   */
  public void testLoad() throws Exception {
    EnunciateConfiguration config = new EnunciateConfiguration(new ArrayList<DeploymentModule>());
    config.load(getClass().getResourceAsStream("basic.config.xml"));
    assertTrue(config.getValidator() instanceof MockValidator);
    assertEquals(3, config.getNamespacesToPrefixes().size());
    assertEquals("pre1", config.getNamespacesToPrefixes().get("urn:net.sf.enunciate.config.TestEnunciateConfiguration.pre1"));
    assertEquals("pre2", config.getNamespacesToPrefixes().get("urn:net.sf.enunciate.config.TestEnunciateConfiguration.pre2"));
    assertEquals("pre3", config.getNamespacesToPrefixes().get("urn:net.sf.enunciate.config.TestEnunciateConfiguration.pre3"));

    //load a file that doesn't validate against the schema.
    config = new EnunciateConfiguration(new ArrayList<DeploymentModule>());
    try {
      config.load(getClass().getResourceAsStream("invalid.config.xml"));
      fail("Should have thrown a validation error.");
    }
    catch (SAXException e) {
      //fall through...
    }

    //validate the "module" element processing...
    

    //validate the explicit "custom-module" processing...

  }

}
