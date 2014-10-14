/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxws;

import junit.framework.Test;
import org.codehaus.enunciate.InAPTTestCase;

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
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.RPCMessageExamples"));
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

  public static Test suite() {
    return createSuite(TestRPCInputMessage.class);
  }
}
