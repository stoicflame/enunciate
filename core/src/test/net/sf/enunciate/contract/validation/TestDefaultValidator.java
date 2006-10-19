package net.sf.enunciate.contract.validation;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.contract.EnunciateContractTestCase;
import net.sf.enunciate.contract.jaxws.EndpointImplementation;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;

import java.util.Collection;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestDefaultValidator extends EnunciateContractTestCase {

  public void testEndpointInterfaceValidity() throws Exception {
    DefaultValidator validator = new DefaultValidator();

    //test validation of JSR 181, secion 3.3
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    EndpointInterface ei = new EndpointInterface(declaration);
    assertTrue("A class not annotated with @WebService shouldn't be a valid endpoint interface (jsr 181: 3.3).", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("net.sf.enunciate.samples.services.InvalidEIReference");
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint implementation with an ei reference to another class shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("net.sf.enunciate.samples.services.UnknownEIReference");
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint implementation with an ei reference to something unknown shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("net.sf.enunciate.samples.services.InterfaceSpecifiedAsImplementation");
    //if an interface is specified as an implementation, it's still an endpoint interface, just not a valid one.
    ei = new EndpointInterface(declaration);
    assertTrue("An interface declaration shouldn't be allowed to specify another endpoint interface (jsr 181: 3.3).", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("net.sf.enunciate.samples.services.WebServiceWithoutUniqueMethodNames");
    //an unknown ei reference is correct, but not valid.
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint without unique web method names shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());
  }

  public void testEndpointImplementationValidity() throws Exception {
    DefaultValidator validator = new DefaultValidator();

    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService"));

    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.NotAWebService");
    EndpointImplementation impl = new EndpointImplementation(declaration, ei) {
    };
    assertTrue("A class not annotated with @WebService shouldn't be seen as an endpoint implementation.", validator.validateEndpointImplementation(impl).hasErrors());

    declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.InvalidEIReference");
    impl = new EndpointImplementation(declaration, ei) {
    };
    assertTrue("A class referencing an ei should be required to implement it.", validator.validateEndpointImplementation(impl).hasErrors());

    declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebServiceImpl");
    impl = new EndpointImplementation(declaration, ei) {
    };
    assertFalse(validator.validateEndpointImplementation(impl).hasErrors());
  }

  public void testWebMethodValidity() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.WebMethodExamples")) {
      @Override
      public boolean isWebMethod(MethodDeclaration method) {
        return true;
      }
    };

    WebMethod privateMethod = null;
    WebMethod protectedMethod = null;
    WebMethod excludedMethod = null;
    WebMethod nonVoidOneWayMethod = null;
    WebMethod exceptionThrowingOneWayMethod = null;
    //todo: support rpc methods
    //WebMethod rpcBareMethod = null;
    WebMethod docBare2ParamMethod = null;
    WebMethod docBare2OutputMethod = null;
    WebMethod docBareWithHeadersMethod = null;
    WebMethod docBareVoidMethod = null;
    WebMethod docBareVoid2OutputMethod = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      if ("privateMethod".equals(webMethod.getSimpleName())) {
        privateMethod = webMethod;
      }
      if ("protectedMethod".equals(webMethod.getSimpleName())) {
        protectedMethod = webMethod;
      }
      if ("excludedMethod".equals(webMethod.getSimpleName())) {
        excludedMethod = webMethod;
      }
      if ("nonVoidOneWayMethod".equals(webMethod.getSimpleName())) {
        nonVoidOneWayMethod = webMethod;
      }
      if ("exceptionThrowingOneWayMethod".equals(webMethod.getSimpleName())) {
        exceptionThrowingOneWayMethod = webMethod;
      }
      //todo: support rpc methods
      //if ("rpcBareMethod".equals(webMethod.getSimpleName())) {
      //  rpcBareMethod = webMethod;
      //}
      if ("docBare2ParamMethod".equals(webMethod.getSimpleName())) {
        docBare2ParamMethod = webMethod;
      }
      if ("docBare2OutputMethod".equals(webMethod.getSimpleName())) {
        docBare2OutputMethod = webMethod;
      }
      if ("docBareWithHeadersMethod".equals(webMethod.getSimpleName())) {
        docBareWithHeadersMethod = webMethod;
      }
      if ("docBareVoidMethod".equals(webMethod.getSimpleName())) {
        docBareVoidMethod = webMethod;
      }
      if ("docBareVoid2OutputMethod".equals(webMethod.getSimpleName())) {
        docBareVoid2OutputMethod = webMethod;
      }
    }

    DefaultValidator validator = new DefaultValidator();
    assertTrue("A private method shouldn't be a web method.", validator.validateWebMethod(privateMethod).hasErrors());
    assertTrue("A protected method shouldn't be a web method.", validator.validateWebMethod(protectedMethod).hasErrors());
    assertTrue("An excluded method shouldn't be a web method.", validator.validateWebMethod(excludedMethod).hasErrors());
    assertTrue("A one-way non-void web method shouldn't be valid.", validator.validateWebMethod(nonVoidOneWayMethod).hasErrors());
    assertTrue("An exception-throwing one-way method shouldn't be valid.", validator.validateWebMethod(exceptionThrowingOneWayMethod).hasErrors());
    //todo: support rpc methods
    //assertTrue(validator.validate(rpcBareMethod).hasErrors(), "An rpc/bare method shouldn't be valid.");
    assertTrue("A doc/bare method shouldn't be valid if it has 2 params.", validator.validateWebMethod(docBare2ParamMethod).hasErrors());
    assertTrue("A doc/bare method shouldn't be valid if it has 2 outputs.", validator.validateWebMethod(docBare2OutputMethod).hasErrors());
    assertFalse("A doc/bare method should be allowed to have headers.", validator.validateWebMethod(docBareWithHeadersMethod).hasErrors());
    assertFalse("A doc/bare void method should be valid.", validator.validateWebMethod(docBareVoidMethod).hasErrors());
    assertTrue("A doc/bare method shouldn't be valid if it has 2 outputs.", validator.validateWebMethod(docBareVoid2OutputMethod).hasErrors());
  }

  public static Test suite() {
    return createSuite(TestDefaultValidator.class);
  }
}
