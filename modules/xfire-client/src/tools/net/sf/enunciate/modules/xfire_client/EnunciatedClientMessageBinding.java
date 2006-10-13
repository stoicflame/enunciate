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

/**
 * The binding for a JAXWS soap message.
 *
 * @author Ryan Heaton
 */
public class EnunciatedClientMessageBinding implements MessageSerializer {

  private final ExplicitWebAnnotations annotations;

  public EnunciatedClientMessageBinding(ExplicitWebAnnotations annotations) {
    this.annotations = annotations;
  }

  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    Service endpoint = context.getService();

    DepthXMLStreamReader dr = new DepthXMLStreamReader(context.getInMessage().getXMLStreamReader());

    if (!STAXUtils.toNextElement(dr)) {
      throw new XFireFault("There must be a method name element.", XFireFault.SENDER);
    }

    OperationInfo op = endpoint.getServiceInfo().getOperation(dr.getLocalName());
    if (op == null) {
      throw new XFireFault("Unknown method: " + dr.getLocalName(), XFireFault.SENDER);
    }

    new EnunciatedClientOperationBinding(annotations, op).readMessage(message, context);
  }

  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    throw new UnsupportedOperationException("Didn't think I needed to implement this.");
  }
}
