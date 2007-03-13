package org.codehaus.enunciate.contract.jaxws;

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

  /**
   * The operation name for this RPC operation to which this message is associated.
   *
   * @return The operation name for this RPC operation to which this message is associated.
   */
  public String getOperationName() {
    return webMethod.getOperationName();
  }

  /**
   * The target namespace of the rpc message.
   *
   * @return The target namespace of the rpc message.
   */
  public String getTargetNamespace() {
    return webMethod.getDeclaringEndpointInterface().getTargetNamespace();
  }

  /**
   * This doesn't have anything to do with the spec, but can be used in case a bean is needed to be
   * generated for an RPC output message.  The bean name will be generated in accordance with the instructions
   * given in the specification that apply to document/literal wrapped response beans.
   *
   * @return A possible response bean name.
   */
  public String getResponseBeanName() {
    String capitalizedName = this.webMethod.getSimpleName();
    capitalizedName = Character.toString(capitalizedName.charAt(0)).toUpperCase() + capitalizedName.substring(1);
    return this.webMethod.getDeclaringEndpointInterface().getPackage().getQualifiedName() + ".jaxws." + capitalizedName + "Response";
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
