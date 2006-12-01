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

  /**
   * The operation name for this RPC operation to which this message is associated.
   *
   * @return The operation name for this RPC operation to which this message is associated.
   */
  public String getOperationName() {
    return webMethod.getOperationName();
  }

  /**
   * This doesn't have anything to do with the spec, but can be used in case a bean is needed to be
   * generated for an RPC input message.  The bean name will be generated in accordance with the instructions
   * given in the specification that apply to document/literal wrapped request beans.
   *
   * @return A possible request bean name.
   */
  public String getRequestBeanName() {
    String capitalizedName = this.webMethod.getSimpleName();
    capitalizedName = Character.toString(capitalizedName.charAt(0)).toUpperCase() + capitalizedName.substring(1);
    return this.webMethod.getDeclaringEndpointInterface().getPackage().getQualifiedName() + ".jaxws." + capitalizedName;
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
