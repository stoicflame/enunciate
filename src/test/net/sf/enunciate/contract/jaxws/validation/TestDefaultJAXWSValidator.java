package net.sf.enunciate.contract.jaxws.validation;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.jaxws.EndpointImplementation;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author Ryan Heaton
 */
public class TestDefaultJAXWSValidator extends EnunciateContractTestCase {

  @Test
  public void testEndpointInterfaceValidity() throws Exception {
    AlwaysValidJAXWSValidator alwaysValidValidator = new AlwaysValidJAXWSValidator();
    DefaultJAXWSValidator validator = new DefaultJAXWSValidator();

    //test validation of JSR 181, secion 3.3
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    EndpointInterface ei = new EndpointInterface(declaration, alwaysValidValidator);
    assertTrue(validator.validate(ei).hasErrors(), "A class not annotated with @WebService shouldn't be a valid endpoint interface (jsr 181: 3.3).");

    declaration = getDeclaration("net.sf.enunciate.samples.services.InvalidEIReference");
    ei = new EndpointInterface(declaration, alwaysValidValidator);
    assertTrue(validator.validate(ei).hasErrors(), "An endpoint implementation with an ei reference to another class shouldn't be valid.");

    declaration = getDeclaration("net.sf.enunciate.samples.services.UnknownEIReference");
    ei = new EndpointInterface(declaration, alwaysValidValidator);
    assertTrue(validator.validate(ei).hasErrors(), "An endpoint implementation with an ei reference to something unknown shouldn't be valid.");

    declaration = getDeclaration("net.sf.enunciate.samples.services.InterfaceSpecifiedAsImplementation");
    //if an interface is specified as an implementation, it's still an endpoint interface, just not a valid one.
    ei = new EndpointInterface(declaration, alwaysValidValidator);
    assertTrue(validator.validate(ei).hasErrors(), "An interface declaration shouldn't be allowed to specify another endpoint interface (jsr 181: 3.3).");

    declaration = getDeclaration("net.sf.enunciate.samples.services.WebServiceWithoutUniqueMethodNames");
    //an unknown ei reference is correct, but not valid.
    ei = new EndpointInterface(declaration, alwaysValidValidator);
    assertTrue(validator.validate(ei).hasErrors(), "An endpoint without unique web method names shouldn't be valid.");
  }

  @Test
  public void testEndpointImplementationValidity() throws Exception {
    AlwaysValidJAXWSValidator alwaysValidValidator = new AlwaysValidJAXWSValidator();
    DefaultJAXWSValidator validator = new DefaultJAXWSValidator();

    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService"), alwaysValidValidator);

    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    EndpointImplementation impl = new EndpointImplementation(declaration, ei) {
    };
    assertTrue(validator.validate(impl).hasErrors(), "A class not annotated with @WebService shouldn't be seen as an endpoint implementation.");

    declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.InvalidEIReference");
    impl = new EndpointImplementation(declaration, ei) {
    };
    assertTrue(validator.validate(impl).hasErrors(), "A class referencing an ei should be required to implement it.");

    declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebServiceImpl");
    impl = new EndpointImplementation(declaration, ei) {
    };
    assertFalse(validator.validate(impl).hasErrors());
  }

  @Test
  public void testWebMethodValidity() throws Exception {

  }
}
