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

import com.sun.mirror.type.TypeMirror;
import org.codehaus.enunciate.contract.jaxws.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxws.WebMessagePart;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.jaxws.WebResult;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;

import javax.xml.namespace.QName;

/**
 * An adapter for the child elements of an RPC request or response bean.
 *
 * @author Ryan Heaton
 */
public class RPCPartChildElementAdapter implements Adaptable, ImplicitChildElement {

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


  /**
   * The xml type of the delegate.
   *
   * @return The xml type of the delegate.
   */
  public XmlType getXmlType() {
    if (this.rpcPart instanceof WebParam) {
      return ((WebParam) this.rpcPart).getXmlType();
    }
    else if (this.rpcPart instanceof WebResult) {
      return ((WebResult) this.rpcPart).getXmlType();
    }
    else {
      return null;
    }
  }

  /**
   * The java type of this part.
   *
   * @return The java type of this part.
   */
  public TypeMirror getType() {
    if (this.rpcPart instanceof WebParam) {
      return ((WebParam) this.rpcPart).getType();
    }
    else if (this.rpcPart instanceof WebResult) {
      return ((WebResult) this.rpcPart).getType();
    }
    else {
      return null;
    }
  }

  // Inherited.
  public boolean isAdapted() {
    return this.rpcPart instanceof Adaptable && ((Adaptable) this.rpcPart).isAdapted();
  }

  // Inherited.
  public AdapterType getAdapterType() {
    return (this.rpcPart instanceof Adaptable) ? ((Adaptable) this.rpcPart).getAdapterType(): null;
  }
}
