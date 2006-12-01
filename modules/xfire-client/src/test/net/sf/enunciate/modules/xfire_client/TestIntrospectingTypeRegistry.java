package net.sf.enunciate.modules.xfire_client;

import junit.framework.TestCase;

import java.util.Arrays;

import net.sf.enunciate.modules.xfire_client.jaxws.DummyMethod;
import net.sf.enunciate.modules.xfire_client.jaxws.DummyMethodXFireType;
import org.codehaus.xfire.aegis.type.TypeMapping;

/**
 * @author Ryan Heaton
 */
public class TestIntrospectingTypeRegistry extends TestCase {

  /**
   * Tests the initialization of the registry.
   */
  public void testInitialization() throws Exception {
    IntrospectingTypeRegistry registry = new IntrospectingTypeRegistry(Arrays.asList(DummyMethod.class));
    String[] uris = registry.getRegisteredEncodingStyleURIs();
    for (String uri : uris) {
      TypeMapping typeMapping = registry.getTypeMapping(uri);
      assertTrue(typeMapping.isRegistered(DummyMethod.class));
      assertTrue(typeMapping.getType(DummyMethod.class) instanceof DummyMethodXFireType);
    }
  }
}
