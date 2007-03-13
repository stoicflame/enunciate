package org.codehaus.enunciate.config;

import junit.framework.TestCase;
import org.codehaus.enunciate.modules.DeploymentModule;
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
    assertEquals("pre1", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre1"));
    assertEquals("pre2", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre2"));
    assertEquals("pre3", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre3"));

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
    ArrayList<DeploymentModule> list = new ArrayList<DeploymentModule>();
    DeploymentModuleOne module1 = new DeploymentModuleOne();
    DeploymentModuleTwo module2 = new DeploymentModuleTwo();
    list.add(module1);
    list.add(module2);
    config = new EnunciateConfiguration(list);
    config.load(getClass().getResourceAsStream("module.config.xml"));
    assertEquals("attribute1", module1.getAttribute());
    assertEquals(3, module1.elementMap.size());
    assertEquals("value1", module1.elementMap.get("element1"));
    assertEquals("value2", module1.elementMap.get("element2"));
    assertEquals("value3", module1.elementMap.get("element3"));
    assertEquals("attribute2", module2.getAttribute());
    assertEquals(3, module2.elementMap.size());
    assertEquals("value4", module2.elementMap.get("element4"));
    assertEquals("value5", module2.elementMap.get("element5"));
    assertEquals("value6", module2.elementMap.get("element6"));

  }

}
