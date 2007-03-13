package org.codehaus.enunciate.modules.jaxws;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxws.*;

import java.util.HashSet;

import com.sun.mirror.declaration.ClassDeclaration;
import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestJAXWSValidator extends InAPTTestCase {

  /**
   * Tests the behavior of conflicting bean names.
   */
  public void testConflictingBeanNames() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.jaxws.BasicEndpointInterface"));
    JAXWSValidator validator = new JAXWSValidator();
    WebMethod alreadyExisting = null;
    WebMethod notConflictingMethod = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("alreadyExisting".equals(webMethod.getSimpleName())) {
        alreadyExisting = webMethod;
      }
      else if ("notConflictingMethod".equals(webMethod.getSimpleName())) {
        notConflictingMethod = webMethod;
      }
    }

    RequestWrapper requestWrapper = new RequestWrapper(alreadyExisting);
    HashSet<String> visited = new HashSet<String>();
    assertTrue("Bean names conflicting with alrady existing classes should not be valid.", validator.validateRequestWrapper(requestWrapper, visited).hasErrors());
    assertTrue(visited.contains(requestWrapper.getRequestBeanName()));

    ResponseWrapper responseWrapper = new ResponseWrapper(alreadyExisting);
    visited.clear();
    assertTrue("Bean names conflicting with alrady existing classes should not be valid.", validator.validateResponseWrapper(responseWrapper, visited).hasErrors());
    assertTrue(visited.contains(responseWrapper.getResponseBeanName()));

    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.jaxws.BasicFault"));
    visited.clear();
    assertTrue("Bean names conflicting with alrady existing classes should not be valid.", validator.validateWebFault(webFault, visited).hasErrors());
    assertTrue(visited.contains(webFault.getImplicitFaultBeanQualifiedName()));

    visited.clear();
    requestWrapper = new RequestWrapper(notConflictingMethod);
    responseWrapper = new ResponseWrapper(notConflictingMethod);
    webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.jaxws.NonConflictingFault"));

    assertFalse(validator.validateRequestWrapper(requestWrapper, visited).hasErrors());
    assertFalse(validator.validateResponseWrapper(responseWrapper, visited).hasErrors());
    assertFalse(validator.validateWebFault(webFault, visited).hasErrors());
    assertEquals(3, visited.size());
    assertTrue("Beans names that have already been used shouldn't be valid.", validator.validateRequestWrapper(requestWrapper, visited).hasErrors());
    assertTrue("Beans names that have already been used shouldn't be valid.", validator.validateResponseWrapper(responseWrapper, visited).hasErrors());
    assertTrue("Beans names that have already been used shouldn't be valid.", validator.validateWebFault(webFault, visited).hasErrors());

  }

  public static Test suite() {
    return createSuite(TestJAXWSValidator.class);
  }
}
