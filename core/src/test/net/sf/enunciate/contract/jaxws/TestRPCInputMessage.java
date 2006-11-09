package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.InAPTTestCase;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 */
public class TestRPCInputMessage extends InAPTTestCase {

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

    RPCInputMessage message = new RPCInputMessage(voidMethod);
    assertEquals(ei.getSimpleName() + ".voidMethod", message.getMessageName());
    assertTrue(message.isInput());
    assertFalse(message.isOutput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    Collection<WebMessagePart> parts = message.getParts();
    assertEquals(1, parts.size());
    Iterator<WebParam> paramIt = voidMethod.getWebParameters().iterator();
    assertTrue(parts.contains(paramIt.next()));

    message = new RPCInputMessage(simpleMethod);
    assertEquals(ei.getSimpleName() + ".simpleMethod", message.getMessageName());
    assertTrue(message.isInput());
    assertFalse(message.isOutput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(2, parts.size());
    paramIt = simpleMethod.getWebParameters().iterator();
    assertTrue(parts.contains(paramIt.next()));
    assertTrue(parts.contains(paramIt.next()));

    message = new RPCInputMessage(withHeader);
    assertEquals(ei.getSimpleName() + ".withHeader", message.getMessageName());
    assertTrue(message.isInput());
    assertFalse(message.isOutput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(1, parts.size());
    paramIt = withHeader.getWebParameters().iterator();
    assertFalse(parts.contains(paramIt.next()));
    assertTrue(parts.contains(paramIt.next()));

    message = new RPCInputMessage(withInOut);
    assertEquals(ei.getSimpleName() + ".withInOut", message.getMessageName());
    assertTrue(message.isInput());
    assertFalse(message.isOutput());
    assertFalse(message.isHeader());
    assertFalse(message.isFault());
    parts = message.getParts();
    assertEquals(2, parts.size());
    paramIt = withInOut.getWebParameters().iterator();
    assertTrue(parts.contains(paramIt.next()));
    assertTrue(parts.contains(paramIt.next()));

  }
}
