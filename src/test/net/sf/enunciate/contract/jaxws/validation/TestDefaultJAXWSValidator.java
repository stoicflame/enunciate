package net.sf.enunciate.contract.jaxws.validation;

import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import static org.testng.Assert.*;

/**
 * @author Ryan Heaton
 */
public class TestDefaultJAXWSValidator extends EnunciateContractTestCase {

  public void testValidity() throws Exception {
    DefaultJAXWSValidator validator = new DefaultJAXWSValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    assertFalse(validator.isEndpointInterface(declaration));
    try {
      new EndpointInterface(declaration, validator);
      fail("Should have thrown an IllegalArgumentException.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }

    declaration = getDeclaration("net.sf.enunciate.samples.services.InterfaceSpecifiedAsImplementation");
    assertTrue(validator.isEndpointInterface(declaration));
    try {
      new EndpointInterface(declaration, validator);
      fail("Shouldn't have allowed an interface declaration to specify another endpoint interface.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }

    declaration = getDeclaration("net.sf.enunciate.samples.services.InvalidEIReference");
    assertTrue(validator.isEndpointInterface(declaration));
    try {
      new EndpointInterface(declaration, validator);
      fail("Shouldn't have allowed a reference to an endpoint interface that is a class.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }

    declaration = getDeclaration("net.sf.enunciate.samples.services.UnknownEIReference");
    assertTrue(validator.isEndpointInterface(declaration));
    try {
      new EndpointInterface(declaration, validator);
      fail("Shouldn't have allowed a reference to an unknown endpoint interface.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }
  }
}
