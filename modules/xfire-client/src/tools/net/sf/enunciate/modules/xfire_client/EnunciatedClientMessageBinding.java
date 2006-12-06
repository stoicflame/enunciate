package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.MessageInfo;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.util.STAXUtils;
import org.codehaus.xfire.util.stax.DepthXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;

/**
 * The binding for an enunciated client-side JAXWS soap message.  The message binding is responsible for
 * reading/writing the outer element of the message (the first element inside the SOAP envelope) and
 * delegating the rest of the work to the operation binding.
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
   * Reads a message.  Because this is a client-side message binding, the messages we will be *reading*
   * are response messages.  Therefore, this method finds the operation in the current exchange, verifies
   * that the element is correct for this operation's *output* message name, and then delegates the rest
   * of the read to the operation binding.
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

    QName responseElementName = dr.getName();

    OperationInfo op = context.getExchange().getOperation();
    if (op == null) {
      throw new XFireFault("No operation in the current exchange: " + responseElementName, XFireFault.SENDER);
    }
    MessageInfo outMessage = op.getOutputMessage();
    if (outMessage == null) {
      throw new XFireFault("No output message in the current operation!", XFireFault.SENDER);
    }
    else if (!responseElementName.equals(outMessage.getName())) {
      throw new XFireFault("Incorrect response message name: " + responseElementName, XFireFault.SENDER);
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
    MessageInfo inMessage = op.getInputMessage();
    if (inMessage == null) {
      throw new XFireFault("No request message was found in the current operation!", XFireFault.RECEIVER);
    }

    new ElementWriter(writer, inMessage.getName());
    getOperationBinding(op).writeMessage(message, writer, context);
    //the xfire SoapSerializer makes the strange assumption that the message serializer doesn't close its element...
    //elementWriter.close();
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
