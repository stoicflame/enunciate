/*
 * Copyright 2006 Web Cohesion
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

import org.codehaus.enunciate.InAPTTestCase;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestRPCOutputMessage extends InAPTTestCase {

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

  public static Test suite() {
    return createSuite(TestRPCOutputMessage.class);
  }
}
