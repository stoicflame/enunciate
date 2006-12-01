package net.sf.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.util.stax.JDOMStreamReader;
import org.codehaus.xfire.util.stax.JDOMStreamWriter;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.exchange.MessageExchange;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.jdom.JDOMWriter;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.ServiceInfo;
import org.codehaus.xfire.service.OperationInfo;
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
    final InMessage inMessage = new InMessage(streamReader);
    MessageContext context = new MessageContext() {
      @Override
      public InMessage getInMessage() {
        return inMessage;
      }


      @Override
      public Service getService() {
        ServiceInfo serviceInfo = new ServiceInfo(null, null);
        serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), null);
        return new Service(serviceInfo);
      }
    };

    final ArrayList<InMessage> messagesReadByOperationBinding = new ArrayList<InMessage>();
    EnunciatedClientMessageBinding binding = new EnunciatedClientMessageBinding(null) {
      @Override
      protected EnunciatedClientOperationBinding getOperationBinding(OperationInfo op) throws XFireFault {
        return new EnunciatedClientOperationBinding(null, op) {
          @Override
          protected WrapperBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          protected WrapperBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
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
    final ArrayList<OutMessage> messagesWrittenByOperationBinding = new ArrayList<OutMessage>();
    EnunciatedClientMessageBinding binding = new EnunciatedClientMessageBinding(null) {
      @Override
      protected EnunciatedClientOperationBinding getOperationBinding(OperationInfo op) throws XFireFault {
        return new EnunciatedClientOperationBinding(null, op) {
          @Override
          protected WrapperBeanInfo getRequestInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          protected WrapperBeanInfo getResponseInfo(OperationInfo op) throws XFireFault {
            return null;
          }

          @Override
          public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
            messagesWrittenByOperationBinding.add(message);
          }
        };
      }
    };

    MessageContext context = new MessageContext();
    MessageExchange exchange = new MessageExchange(context);
    ServiceInfo serviceInfo = new ServiceInfo(null, null);
    OperationInfo operationInfo = serviceInfo.addOperation(new QName("urn:some-ns", "some-op"), null);
    exchange.setOperation(operationInfo);
    context.setExchange(exchange);
    OutMessage outMessage = new OutMessage("http://localhost:12345");
    Element element = new Element("outerElement");
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
