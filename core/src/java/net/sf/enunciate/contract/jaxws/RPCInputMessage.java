package net.sf.enunciate.contract.jaxws;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An RPC-style input message.
 *
 * @author Ryan Heaton
 */
public class RPCInputMessage implements WebMessage {

  private final WebMethod webMethod;

  public RPCInputMessage(WebMethod webMethod) {
    this.webMethod = webMethod;

    if (!(webMethod.getSoapBindingStyle() == SOAPBinding.Style.RPC)) {
      throw new IllegalArgumentException("An RPC-style input message cannot be created from a web method of DOCUMENT-style");
    }
  }

  // Inherited.
  public String getMessageName() {
    return webMethod.getDeclaringEndpointInterface().getSimpleName() + "." + webMethod.getSimpleName();
  }

  // Inherited.
  public String getMessageDocs() {
    String docs = "Input message for operation \"" + webMethod.getOperationName() + "\".";
    String methodDocs = webMethod.getJavaDoc().toString();
    if (methodDocs.trim().length() > 0) {
      docs += " (" + methodDocs.trim() + ")";
    }
    return docs;
  }

  // Inherited.
  public boolean isInput() {
    return true;
  }

  // Inherited.
  public boolean isOutput() {
    return false;
  }

  // Inherited.
  public boolean isHeader() {
    return false;
  }

  // Inherited.
  public boolean isFault() {
    return false;
  }

  // Inherited.
  public Collection<WebMessagePart> getParts() {
    ArrayList<WebMessagePart> parts = new ArrayList<WebMessagePart>();
    for (WebParam webParam : this.webMethod.getWebParameters()) {
      if ((!webParam.isHeader()) && (webParam.isInput())) {
        parts.add(webParam);
      }
    }
    return parts;
  }

}
