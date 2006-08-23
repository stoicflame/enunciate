package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.validation.ValidationException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;

/**
 * Test suite for the web service decoration.
 *
 * @author Ryan Heaton
 */
public class TestEndpointInterface extends EnunciateContractTestCase {

  @Test
  public void testTargetNamespace() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertEquals(new EndpointInterface(declaration).getTargetNamespace(),
                 "http://services.samples.enunciate.sf.net/",
                 "calculated namespace doesn't conform to JSR 181: 3.2");

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

  @Test
  public void testGetWebMethods() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    Collection<WebMethod> webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(webMethods.size(), 1);
    assertEquals(webMethods.iterator().next().getSimpleName(), "myPublicMethod");

    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(webMethods.size(), 2);
    Iterator<WebMethod> it = webMethods.iterator();
    WebMethod first = it.next();
    WebMethod second = it.next();
    assertTrue("myImplicitlyPublicMethod".equals(first.getSimpleName()) || "myExplicitlyPublicMethod".equals(first.getSimpleName()));
    assertTrue("myImplicitlyPublicMethod".equals(second.getSimpleName()) || "myExplicitlyPublicMethod".equals(second.getSimpleName()));

    declaration = getDeclaration("net.sf.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(webMethods.size(), 4);
  }

  /**
   * Tests the attributes of an ei.
   */
  @Test
  public void testAttributes() throws Exception {
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    EndpointInterface annotated = new EndpointInterface(declaration);
    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    EndpointInterface notAnnotated = new EndpointInterface(declaration);

    assertEquals(annotated.getPortTypeName(), "annotated-web-service", "The port type name of the web service should be customized by the annotation (JSR 181: 3.4)");
    assertEquals(notAnnotated.getPortTypeName(), "NoNamespaceWebService", "The port type name of the web service should be the simple name if not annotated (JSR 181: 3.4)");
  }

}
