package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.InAPTTestCase;

import java.util.Collection;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestWebMethod extends InAPTTestCase {

  public void testName() throws Exception {
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
    assertEquals("The operation name should be able to be customized with the annotation.", "special-operation-name", specialNameMethod.getOperationName());
    assertNotNull(docBareVoidMethod);
    assertEquals("The operation name should default to the simple name.", "docBareVoidMethod", docBareVoidMethod.getOperationName());
  }

  public static Test suite() {
    return createSuite(TestWebMethod.class);
  }

}
