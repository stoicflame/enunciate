package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.contract.jaxws.ImplicitChildElement;
import net.sf.enunciate.contract.jaxws.WebMessagePart;

import javax.xml.namespace.QName;

/**
 * An adapter for the child elements of an RPC request or response bean.
 *
 * @author Ryan Heaton
 */
public class RPCPartChildElementAdapter implements ImplicitChildElement {

  private final WebMessagePart rpcPart;

  public RPCPartChildElementAdapter(WebMessagePart rpcPart) {
    this.rpcPart = rpcPart;
  }

  /**
   * The min occurs for an rpc child element is undefined (there is no schema for this element).
   *
   * @throws UnsupportedOperationException
   */
  public int getMinOccurs() {
    throw new UnsupportedOperationException();
  }

  /**
   * The max occurs for an rpc child element is undefined (there is no schema for this element).
   *
   * @throws UnsupportedOperationException
   */
  public String getMaxOccurs() {
    throw new UnsupportedOperationException();
  }

  /**
   * The type qname of the rpc child element is undefined (there is no schema for this element).
   *
   * @throws UnsupportedOperationException
   */
  public QName getTypeQName() {
    throw new UnsupportedOperationException();
  }

  /**
   * The child element name is the part name.
   *
   * @return The child element name is the part name.
   */
  public String getElementName() {
    return this.rpcPart.getPartName();
  }

  /**
   * The element docs are the part docs.
   *
   * @return The element docs are the part docs.
   */
  public String getElementDocs() {
    return this.rpcPart.getPartDocs();
  }
  
}
