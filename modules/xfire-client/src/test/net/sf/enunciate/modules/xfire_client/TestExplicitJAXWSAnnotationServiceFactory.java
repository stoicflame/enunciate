package net.sf.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.transport.TransportManager;

/**
 * @author Ryan Heaton
 */
public class TestExplicitJAXWSAnnotationServiceFactory extends TestCase {

  /**
   * test the methods that set up a service.
   */
  public void testServiceSetup() throws Exception {
    XFire xFire = XFireFactory.newInstance().getXFire();
    TransportManager transportManager = xFire.getTransportManager();
    EnunciatedClientBindingProvider clientBP = new EnunciatedClientBindingProvider(new DefaultTypeMappingRegistry(true));
    ExplicitJAXWSAnnotationServiceFactory factory = new ExplicitJAXWSAnnotationServiceFactory(new ExplicitWebAnnotations(), transportManager, clientBP);
    assertTrue(factory.getSerializer(null) instanceof EnunciatedClientMessageBinding);

    //todo: find a good way to test the other stuff.
  }
}
