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
    assertEquals("http://services.samples.enunciate.sf.net/",
                 new EndpointInterface(declaration, validator).getTargetNamespace(),
                 "calculated namespace doesn't conform to JSR 181: 3.2");

    declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    assertEquals("http://enunciate.sf.net/samples/contract", new EndpointInterface(declaration, validator).getTargetNamespace());

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
        throw new ValidationException("invalid");
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
    assertEquals(1, webMethods.size());
    assertEquals("myPublicMethod", webMethods.iterator().next().getSimpleName());

    declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    webMethods = new EndpointInterface(declaration, validator).getWebMethods();
    assertEquals(2, webMethods.size());
    Iterator<WebMethod> it = webMethods.iterator();
    WebMethod first = it.next();
    WebMethod second = it.next();
    assertTrue("myImplicitlyPublicMethod".equals(first.getSimpleName()) || "myExplicitlyPublicMethod".equals(first.getSimpleName()));
    assertTrue("myImplicitlyPublicMethod".equals(second.getSimpleName()) || "myExplicitlyPublicMethod".equals(second.getSimpleName()));

    declaration = getDeclaration("net.sf.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    webMethods = new EndpointInterface(declaration, validator).getWebMethods();
    assertEquals(4, webMethods.size());
  }

}
