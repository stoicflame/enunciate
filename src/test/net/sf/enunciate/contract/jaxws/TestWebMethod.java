package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.validation.AlwaysValidValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestWebMethod extends EnunciateContractTestCase {

  @Test
  public void testName() throws Exception {
    AlwaysValidValidator alwaysValidValidator = new AlwaysValidValidator();
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.WebMethodExamples"));

    WebMethod specialNameMethod = null;
    WebMethod docBareVoidMethod = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      if ("specialNameMethod".equals(webMethod.getSimpleName())) {
        specialNameMethod = webMethod;
      }
      if ("docBareVoidMethod".equals(webMethod.getSimpleName())) {
        docBareVoidMethod = webMethod;
      }
    }

    assertNotNull(specialNameMethod);
    assertEquals(specialNameMethod.getOperationName(), "special-operation-name", "The operation name should be able to be customized with the annotation.");
    assertNotNull(docBareVoidMethod);
    assertEquals(docBareVoidMethod.getOperationName(), "docBareVoidMethod", "The operation name should default to the simple name.");
  }
}
