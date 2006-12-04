package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.contract.jaxws.ImplicitChildElement;
import net.sf.enunciate.contract.jaxws.ImplicitRootElement;
import net.sf.enunciate.contract.jaxws.RPCInputMessage;
import net.sf.enunciate.contract.jaxws.WebMessagePart;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * For all intents and purposes, an RPC/literal input message can be processed (at runtime) as if it is
 * a document/literal request message, as long as the outer element has the correct namespace and name.
 * Enunciate, therefore, uses request and response beans to (de)serialize the RPC messages.  This class
 * is an adapter for an RPC input message to a document/literal request bean.
 *
 * @author Ryan Heaton
 */
public class RPCInputRequestBeanAdapter implements ImplicitRootElement {

  private final RPCInputMessage rpcMessage;

  public RPCInputRequestBeanAdapter(RPCInputMessage rpcMessage) {
    this.rpcMessage = rpcMessage;
  }

  /**
   * The name of the bean for this adapter.
   *
   * @return The name of the bean for this adapter.
   */
  public String getRequestBeanName() {
    return this.rpcMessage.getRequestBeanName();
  }

  /**
   * The rpc parts adapted to be "child elements."
   *
   * @return The adapted child elements.
   */
  public Collection<ImplicitChildElement> getChildElements() {
    Collection<WebMessagePart> parts = rpcMessage.getParts();
    ArrayList<ImplicitChildElement> childElements = new ArrayList<ImplicitChildElement>();
    for (WebMessagePart part : parts) {
      childElements.add(new RPCPartChildElementAdapter(part));
    }
    return childElements;
  }

  /**
   * The element name of the message is the operation name.
   *
   * @return The element name of the message is the operation name.
   */
  public String getElementName() {
    return this.rpcMessage.getOperationName();
  }

  /**
   * The element namespace for the implicit rpc element.
   *
   * @return The element namespace for the implicit rpc element.
   */
  public String getElementNamespace() {
    return this.rpcMessage.getTargetNamespace();
  }

  /**
   * The element docs are the message docs.
   *
   * @return The element docs are the message docs.
   */
  public String getElementDocs() {
    return this.rpcMessage.getMessageDocs();
  }

  /**
   * The "type" for an rpc message is undefined.
   *
   * @return null
   */
  public QName getTypeQName() {
    return null;
  }
}
