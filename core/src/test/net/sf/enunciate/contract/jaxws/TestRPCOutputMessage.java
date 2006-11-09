package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.InAPTTestCase;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 */
public class TestRPCOutputMessage extends InAPTTestCase {

  /**
   * tests the names and properties of an rpc-style input message.
   */
  public void testNamesAndProperties() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.RPCMessageExamples"));
    WebMethod voidMethod = null;
    WebMethod simpleMethod = null;
    WebMethod withHeader = null;
    WebMethod withInOut = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("voidMethod".equals(webMethod.getSimpleName())) {
        voidMethod = webMethod;
      }
      else if ("simpleMethod".equals(webMethod.getSimpleName())) {
        simpleMethod = webMethod;
      }
      else if ("withHeader".equals(webMethod.getSimpleName())) {
        withHeader = webMethod;
      }
      else if ("withInOut".equals(webMethod.getSimpleName())) {
        withInOut = webMethod;
      }
    }

    RPCOutputMessage message = new RPCOutputMessage(voidMethod);
    assertEquals(ei.getSimpleName() + ".voidMethodResponse", message.getMessageName());
    assertTrue(message.isOutput());
    assertFalse(message.isInput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    Collection<WebMessagePart> parts = message.getParts();
    assertEquals(0, parts.size());

    message = new RPCOutputMessage(simpleMethod);
    assertEquals(ei.getSimpleName() + ".simpleMethodResponse", message.getMessageName());
    assertTrue(message.isOutput());
    assertFalse(message.isInput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(1, parts.size());
    assertTrue(parts.contains(simpleMethod.getWebResult()));

    message = new RPCOutputMessage(withHeader);
    assertEquals(ei.getSimpleName() + ".withHeaderResponse", message.getMessageName());
    assertTrue(message.isOutput());
    assertFalse(message.isInput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(1, parts.size());
    assertTrue(parts.contains(withHeader.getWebResult()));

    message = new RPCOutputMessage(withInOut);
    assertEquals(ei.getSimpleName() + ".withInOutResponse", message.getMessageName());
    assertTrue(message.isOutput());
    assertFalse(message.isInput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(2, parts.size());
    Iterator<WebParam> paramIt = withInOut.getWebParameters().iterator();
    assertTrue(parts.contains(paramIt.next()));
    assertTrue(parts.contains(withInOut.getWebResult()));

  }
}
