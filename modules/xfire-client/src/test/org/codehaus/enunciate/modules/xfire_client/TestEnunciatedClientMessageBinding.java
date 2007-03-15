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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageExchange;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.ServiceInfo;
import org.codehaus.xfire.util.stax.JDOMStreamReader;
import org.codehaus.xfire.util.stax.JDOMStreamWriter;
import org.jdom.Element;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class TestEnunciatedClientMessageBinding extends TestCase {

  /**
   * tests reading the message.
   */
  public void testReadMessage() throws Exception {
    JDOMStreamReader streamReader = new JDOMStreamReader(new Element("some-op", "urn:some-ns"));
    InMessage inMessage = new InMessage(streamReader);
    MessageContext context = new MessageContext();
    MessageExchange exchange = new MessageExchange(context);
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo opInfo = serviceInfo.addOperation(new QName("urn:doesntmatter", "anything"), null);
    opInfo.setOutputMessage(opInfo.createMessage(new QName("urn:some-ns", "some-op")));
    exchange.setOperation(opInfo);
    exchange.setInMessage(inMessage);
    context.setExchange(exchange);

    final ArrayList<InMessage> messagesReadByOperationBinding = new ArrayList<InMessage>();
    EnunciatedClientMessageBinding binding = new EnunciatedClientMessageBinding(null) {
      @Override
      protected EnunciatedClientOperationBinding getOperationBinding(OperationInfo op) throws XFireFault {
        return new EnunciatedClientOperationBinding(null, op) {
          @Override
          protected OperationBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          protected OperationBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          public void readMessage(InMessage message, MessageContext context) throws XFireFault {
            messagesReadByOperationBinding.add(message);
          }
        };
      }
    };

    binding.readMessage(inMessage, context);
    assertEquals(1, messagesReadByOperationBinding.size());
    assertSame(inMessage, messagesReadByOperationBinding.get(0));
  }

  /**
   * tests writing a message.
   */
  public void testWriteMessage() throws Exception {
    MessageContext context = new MessageContext();
    MessageExchange exchange = new MessageExchange(context);
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo operationInfo = serviceInfo.addOperation(new QName("urn:doesntmatter", "anything"), null);
    operationInfo.setInputMessage(operationInfo.createMessage(new QName("urn:some-ns", "some-op")));
    exchange.setOperation(operationInfo);
    context.setExchange(exchange);
    OutMessage outMessage = new OutMessage("http://localhost:12345");
    Element element = new Element("soapenvelope");

    final ArrayList<OutMessage> messagesWrittenByOperationBinding = new ArrayList<OutMessage>();
    EnunciatedClientMessageBinding binding = new EnunciatedClientMessageBinding(null) {
      @Override
      protected EnunciatedClientOperationBinding getOperationBinding(OperationInfo op) throws XFireFault {
        return new EnunciatedClientOperationBinding(null, op) {
          @Override
          protected OperationBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          protected OperationBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
            messagesWrittenByOperationBinding.add(message);
          }
        };
      }
    };

    binding.writeMessage(outMessage, new JDOMStreamWriter(element), context);
    assertEquals(1, messagesWrittenByOperationBinding.size());
    assertSame(outMessage, messagesWrittenByOperationBinding.get(0));
    List childElements = element.getChildren();
    assertEquals(1, childElements.size());
    Element childElement = ((Element) childElements.get(0));
    assertEquals("some-op", childElement.getName());
    assertEquals("urn:some-ns", childElement.getNamespaceURI());
  }
}
