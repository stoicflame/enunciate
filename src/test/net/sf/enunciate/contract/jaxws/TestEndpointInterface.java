package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.validation.AlwaysValidJAXWSValidator;
import net.sf.enunciate.contract.jaxws.validation.DefaultJAXWSValidator;
import net.sf.enunciate.contract.jaxws.validation.JAXWSValidator;
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
    JAXWSValidator validator = new AlwaysValidJAXWSValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertEquals(new EndpointInterface(declaration, validator).getTargetNamespace(),
                 "http://services.samples.enunciate.sf.net/",
                 "calculated namespace doesn't conform to JSR 181: 3.2");

    declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    assertEquals(new EndpointInterface(declaration, validator).getTargetNamespace(), "http://enunciate.sf.net/samples/contract");

    declaration = getDeclaration("NoPackageWebService");
    try {
      new EndpointInterface(declaration, validator).calculateNamespaceURI();
      fail("Shouldn't have been able to calculate the namespace URI.");
    }
    catch (ValidationException e) {
      //fall through.
    }
  }

  @Test
  public void testConstructInvalidEndpointInterface() throws Exception {
    JAXWSValidator validator = new DefaultJAXWSValidator() {
      //Inherited.
      @Override
      public ValidationResult validate(EndpointInterface ei) throws ValidationException {
        throw new ValidationException(ei.getPosition(), "invalid");
      }
    };

    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    try {
      new EndpointInterface(declaration, validator);
      fail("Should have thrown a validation exception.");
    }
    catch (ValidationException e) {
      //fall through.
    }
  }

  @Test
  public void testGetWebMethods() throws Exception {
    JAXWSValidator validator = new AlwaysValidJAXWSValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    Collection<WebMethod> webMethods = new EndpointInterface(declaration, validator).getWebMethods();
    assertEquals(webMethods.size(), 1);
    assertEquals(webMethods.iterator().next().getSimpleName(), "myPublicMethod");

    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    webMethods = new EndpointInterface(declaration, validator).getWebMethods();
    assertEquals(webMethods.size(), 2);
    Iterator<WebMethod> it = webMethods.iterator();
    WebMethod first = it.next();
    WebMethod second = it.next();
    assertTrue("myImplicitlyPublicMethod".equals(first.getSimpleName()) || "myExplicitlyPublicMethod".equals(first.getSimpleName()));
    assertTrue("myImplicitlyPublicMethod".equals(second.getSimpleName()) || "myExplicitlyPublicMethod".equals(second.getSimpleName()));

    declaration = getDeclaration("net.sf.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    webMethods = new EndpointInterface(declaration, validator).getWebMethods();
    assertEquals(webMethods.size(), 4);
  }

  /**
   * Tests the attributes of an ei.
   */
  @Test
  public void testAttributes() throws Exception {
    JAXWSValidator validator = new AlwaysValidJAXWSValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    EndpointInterface annotated = new EndpointInterface(declaration, validator);
    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    EndpointInterface notAnnotated = new EndpointInterface(declaration, validator);

    assertEquals(annotated.getPortTypeName(), "annotated-web-service", "The port type name of the web service should be customized by the annotation (JSR 181: 3.4)");
    assertEquals(notAnnotated.getPortTypeName(), "NoNamespaceWebService", "The port type name of the web service should be the simple name if not annotated (JSR 181: 3.4)");
  }

}
