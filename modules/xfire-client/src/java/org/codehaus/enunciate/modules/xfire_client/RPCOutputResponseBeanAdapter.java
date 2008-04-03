/*
 * Copyright 2006-2008 Web Cohesion
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

import org.codehaus.enunciate.contract.jaxws.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxws.ImplicitRootElement;
import org.codehaus.enunciate.contract.jaxws.RPCOutputMessage;
import org.codehaus.enunciate.contract.jaxws.WebMessagePart;

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
public class RPCOutputResponseBeanAdapter implements ImplicitRootElement {

  private final RPCOutputMessage rpcMessage;

  public RPCOutputResponseBeanAdapter(RPCOutputMessage rpcMessage) {
    this.rpcMessage = rpcMessage;
  }

  /**
   * The name of the bean for this adapter.
   *
   * @return The name of the bean for this adapter.
   */
  public String getResponseBeanName() {
    return this.rpcMessage.getResponseBeanName();
  }

  /**
   * The "child elements" are the web parameters to the message.
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
