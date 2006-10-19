package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.validation.ValidationException;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

/**
 * Test suite for the web service decoration.
 *
 * @author Ryan Heaton
 */
public class TestEndpointInterface extends EnunciateContractTestCase {

  public void testTargetNamespace() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertEquals("calculated namespace doesn't conform to JSR 181: 3.2", "http://services.samples.enunciate.sf.net/", new EndpointInterface(declaration).getTargetNamespace()
    );

    declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    assertEquals(new EndpointInterface(declaration).getTargetNamespace(), "http://enunciate.sf.net/samples/contract");

    declaration = getDeclaration("NoPackageWebService");
    try {
      new EndpointInterface(declaration).calculateNamespaceURI();
      fail("Shouldn't have been able to calculate the namespace URI.");
    }
    catch (ValidationException e) {
      //fall through.
    }
  }

  public void testGetWebMethods() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    Collection<WebMethod> webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(1, webMethods.size());
    assertEquals("myPublicMethod", webMethods.iterator().next().getSimpleName());

    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(2, webMethods.size());
    Iterator<WebMethod> it = webMethods.iterator();
    WebMethod first = it.next();
    WebMethod second = it.next();
    assertTrue("myImplicitlyPublicMethod".equals(first.getSimpleName()) || "myExplicitlyPublicMethod".equals(first.getSimpleName()));
    assertTrue("myImplicitlyPublicMethod".equals(second.getSimpleName()) || "myExplicitlyPublicMethod".equals(second.getSimpleName()));

    declaration = getDeclaration("net.sf.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(4, webMethods.size());
  }

  /**
   * Tests the attributes of an ei.
   */
  public void testAttributes() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    EndpointInterface annotated = new EndpointInterface(declaration);
    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    EndpointInterface notAnnotated = new EndpointInterface(declaration);

    assertEquals("The port type name of the web service should be customized by the annotation (JSR 181: 3.4)", "annotated-web-service", annotated.getPortTypeName());
    assertEquals("The port type name of the web service should be the simple name if not annotated (JSR 181: 3.4)", "NoNamespaceWebService", notAnnotated.getPortTypeName());
  }

  public static Test suite() {
    return createSuite(TestEndpointInterface.class);
  }

}
