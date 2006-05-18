package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxws.validation.DefaultJAXWSValidator;
import net.sf.enunciate.contract.jaxws.validation.JAXWSValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * Test suite for the web service decoration.
 *
 * @author Ryan Heaton
 */
public class TestEndpointInterface extends EnunciateContractTestCase {

  @Test
  public void testTargetNamespace() throws Exception {
    JAXWSValidator validator = new DefaultJAXWSValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertEquals("http://services.samples.enunciate.sf.net/", new EndpointInterface(declaration, validator).getTargetNamespace());

    declaration = getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService");
    assertEquals("http://enunciate.sf.net/samples/contract", new EndpointInterface(declaration, validator).getTargetNamespace());
  }

  @Test
  public void testConstructInvalidEndpointInterface() throws Exception {
    JAXWSValidator validator = new DefaultJAXWSValidator() {
      //Inherited.
      @Override
      public void validate(EndpointInterface ei) throws ValidationException {
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

}
