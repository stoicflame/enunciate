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

package org.codehaus.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.MessageInfo;
import org.codehaus.xfire.util.STAXUtils;
import org.codehaus.xfire.util.stax.DepthXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collection;
import java.util.HashMap;

/**
 * The message binding for a JAXWS soap message.  The message binding is responsible for
 * reading/writing the outer element of the message (the first element inside the SOAP envelope) and
 * delegating the rest of the work to the operation binding.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSMessageBinding implements MessageSerializer {

  private final HashMap<OperationInfo, EnunciatedJAXWSOperationBinding> op2Binding = new HashMap<OperationInfo, EnunciatedJAXWSOperationBinding>();

  /**
   * Reads a message.  Because this is a server-side message binding, the messages we will be *reading*
   * are request messages.  Therefore, this method finds the operation whose input message name matches
   * the name of the request element, sets that operation in the context, looks up the binding for that
   * operation, and delegates the rest of the read to the binding. 
   *
   * @param message The message to read.
   * @param context The context.
   * @see org.codehaus.xfire.jaxws.JAXWSBinding
   */
  public void readMessage(InMessage message, MessageContext context) throws XFireFault {
    Service endpoint = context.getService();

    DepthXMLStreamReader dr = new DepthXMLStreamReader(message.getXMLStreamReader());

    if (!STAXUtils.toNextElement(dr)) {
      throw new XFireFault("There must be a method name element.", XFireFault.SENDER);
    }

    QName messageName = dr.getName();
    OperationInfo opInfo = findOperation(endpoint, messageName);
    if (opInfo == null) {
      throw new XFireFault("Unknown operation: " + messageName, XFireFault.SENDER);
    }

    context.getExchange().setOperation(opInfo);

    EnunciatedJAXWSOperationBinding binding = op2Binding.get(opInfo);
    if (binding == null) {
      binding = new EnunciatedJAXWSOperationBinding(opInfo);
      op2Binding.put(opInfo, binding);
    }

    binding.readMessage(message, context);
  }

  /**
   * Writes a message by looking up the operation in the current context and delegating to its associated binding.
   *
   * @param message The message to write.
   * @param writer The writer to which to write.
   * @param context The context
   */
  public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context) throws XFireFault {
    OperationInfo op = context.getExchange().getOperation();
    if (op == null) {
      throw new XFireFault("No operation was found in the current context!", XFireFault.RECEIVER);
    }

    MessageInfo outMessage = op.getOutputMessage();
    if (outMessage == null) {
      throw new XFireFault("No output message was found in the current operation!", XFireFault.RECEIVER);
    }

    EnunciatedJAXWSOperationBinding binding = op2Binding.get(op);
    if (binding == null) {
      binding = new EnunciatedJAXWSOperationBinding(op);
      op2Binding.put(op, binding);
    }

    //in this case, the operation binding does the actual work of writing the outer element...
    binding.writeMessage(message, writer, context);
  }

  /**
   * Finds the operation for the specified input message name.
   *
   * @param endpoint The endpoint.
   * @param inMessageName The input message name.
   * @return The operation, or null if not found.
   */
  protected OperationInfo findOperation(Service endpoint, QName inMessageName) throws XFireFault {
    Collection operations = endpoint.getServiceInfo().getOperations();
    for (Object o : operations) {
      OperationInfo opInfo = ((OperationInfo) o);
      MessageInfo inMessageInfo = opInfo.getInputMessage();
      if ((inMessageInfo != null) && (inMessageInfo.getName().equals(inMessageName))) {
        return opInfo;
      }
    }

    return null;
  }

}
