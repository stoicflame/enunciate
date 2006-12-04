package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.util.STAXUtils;
import org.codehaus.xfire.util.stax.DepthXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collection;

/**
 * The binding for a JAXWS soap message.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSMessageBinding implements MessageSerializer {

  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    Service endpoint = context.getService();

    DepthXMLStreamReader dr = new DepthXMLStreamReader(message.getXMLStreamReader());

    if (!STAXUtils.toNextElement(dr)) {
      throw new XFireFault("There must be a method name element.", XFireFault.SENDER);
    }

    QName inMessageName = dr.getName();

    Collection operations = endpoint.getServiceInfo().getOperations();
    for (Object o : operations) {
      OperationInfo op = (OperationInfo) o;
      if ((op.getName().equals(inMessageName)) || ((op.getInputMessage() != null) && (op.getInputMessage().getName().equals(inMessageName)))) {
        context.getExchange().setOperation(op);
        //todo: store a map of the operation bindings...
        new EnunciatedJAXWSOperationBinding(op).readMessage(message, context);
        return;
      }
    }

    throw new XFireFault("Unknown method: " + dr.getLocalName(), XFireFault.SENDER);
  }

  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    throw new UnsupportedOperationException("Didn't think I needed to implement this.");
  }
}
