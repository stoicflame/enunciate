package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeCreator;
import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethod;
import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethodXFireType;

/**
 * @author Ryan Heaton
 */
public class TestIntrospectingTypeCreator extends TestCase {

  /**
   * tests creating a type.
   */
  public void testCreateType() throws Exception {
    DefaultTypeMappingRegistry registry = new DefaultTypeMappingRegistry(true);
    TypeCreator typeCreator = registry.getTypeCreator();
    typeCreator.setTypeMapping(registry.getDefaultTypeMapping());
    IntrospectingTypeCreator introspectingCreator = new IntrospectingTypeCreator(typeCreator);
    assertTrue(introspectingCreator.createType(GeneratedWrapperBeanExample.class) instanceof GeneratedWrapperBeanType);
    assertTrue(introspectingCreator.createType(DummyMethod.class) instanceof DummyMethodXFireType);
    assertNotNull(introspectingCreator.createType(String.class));
  }
}
