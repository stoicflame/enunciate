package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.util.STAXUtils;
import org.codehaus.xfire.util.stax.DepthXMLStreamReader;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;

/**
 * The binding for an enunciated client-side JAXWS soap message.
 *
 * @author Ryan Heaton
 */
public class EnunciatedClientMessageBinding implements MessageSerializer {

  private final ExplicitWebAnnotations annotations;
  private final HashMap operationBindings = new HashMap();

  /**
   * Construct a new enunciated client-side message binding.  Annotations are needed for (de)serialization.
   *
   * @param annotations The metadata to use to (de)serialize the messages.
   */
  public EnunciatedClientMessageBinding(ExplicitWebAnnotations annotations) {
    this.annotations = annotations;
  }

  /**
   * Read a message.  Peeks at the outer element to determine the operation that is being invoked, then delegates
   * to the operation binding to read the message.
   *
   * @param message The message to read.
   * @param context The context.
   * @see org.codehaus.xfire.jaxws.JAXWSBinding
   */
  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    DepthXMLStreamReader dr = new DepthXMLStreamReader(context.getInMessage().getXMLStreamReader());
    if (!STAXUtils.toNextElement(dr)) {
      throw new XFireFault("There must be a method name element.", XFireFault.SENDER);
    }

    Service endpoint = context.getService();
    OperationInfo op = endpoint.getServiceInfo().getOperation(dr.getLocalName());
    if (op == null) {
      throw new XFireFault("Unknown method: " + dr.getLocalName(), XFireFault.SENDER);
    }

    getOperationBinding(op).readMessage(message, context);
  }

  /**
   * Writes a message.  Delegates to the operation binding of the operation in the current message exchange.
   *
   * @param message The message to write.
   * @param writer The writer to which to write.
   * @param context The context
   */
  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    OperationInfo op = context.getExchange().getOperation();
    try {
      writer.writeStartElement(op.getQName().getNamespaceURI(), op.getQName().getLocalPart());
    }
    catch (XMLStreamException e) {
      throw new XFireFault("Unable to write the operation element.", e, XFireFault.RECEIVER);
    }

    getOperationBinding(op).writeMessage(message, writer, context);
  }

  /**
   * Gets the operation binding for the specified operation info.
   *
   * @param op The operation info.
   * @return The operation binding for the info.
   */
  protected EnunciatedClientOperationBinding getOperationBinding(OperationInfo op) throws XFireFault {
    EnunciatedClientOperationBinding operationBinding = (EnunciatedClientOperationBinding) operationBindings.get(op);
    if (operationBinding == null) {
      operationBinding = new EnunciatedClientOperationBinding(annotations, op);
      operationBindings.put(op, operationBinding);
    }
    return operationBinding;
  }

}
