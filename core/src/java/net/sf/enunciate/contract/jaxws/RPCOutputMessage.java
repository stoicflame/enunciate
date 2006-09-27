package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.VoidType;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An RPC output message.
 *
 * @author Ryan Heaton
 */
public class RPCOutputMessage implements WebMessage {

  private final WebMethod webMethod;

  public RPCOutputMessage(WebMethod webMethod) {
    this.webMethod = webMethod;

    if (!(webMethod.getSoapBindingStyle() == SOAPBinding.Style.RPC)) {
      throw new IllegalArgumentException("An RPC-style output message cannot be created from a web method of DOCUMENT-style");
    }
  }

  // Inherited.
  public String getMessageName() {
    return webMethod.getDeclaringEndpointInterface().getSimpleName() + "." + this.webMethod.getSimpleName() + "Response";
  }

  // Inherited.
  public String getMessageDocs() {
    String docs = "Output message for operation \"" + webMethod.getOperationName() + "\".";
    String methodDocs = webMethod.getJavaDoc().toString();
    if (methodDocs.trim().length() > 0) {
      docs += " (" + methodDocs.trim() + ")";
    }
    return docs;
  }

  // Inherited.
  public boolean isInput() {
    return false;
  }

  // Inherited.
  public boolean isOutput() {
    return true;
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
      if ((webParam.isOutput()) && (!webParam.isHeader())) {
        parts.add(webParam);
      }
    }

    TypeMirror returnType = this.webMethod.getReturnType();
    if (!(returnType instanceof VoidType)) {
      parts.add(this.webMethod.getWebResult());
    }

    return parts;
  }

}
