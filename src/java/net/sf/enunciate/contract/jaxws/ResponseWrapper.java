package net.sf.enunciate.contract.jaxws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A response wrapper for a web method in document/literal wrapped style.
 *
 * @author Ryan Heaton
 */
public class ResponseWrapper implements ComplexWebMessage, WebMessagePart {

  private WebMethod webMethod;

  /**
   * @param webMethod The web method to wrap.
   */
  protected ResponseWrapper(WebMethod webMethod) {
    this.webMethod = webMethod;
    webMethod.getDeclaringEndpointInterface().getValidator().validate(this);
  }

  /**
   * Get the web method to which this response is associated.
   *
   * @return The web method to which this response is associated.
   */
  public WebMethod getWebMethod() {
    return webMethod;
  }

  /**
   * The local name of the output.
   *
   * @return The local name of the output.
   */
  public String getName() {
    String name = webMethod.getSimpleName() + "Response";

    javax.xml.ws.ResponseWrapper annotation = webMethod.getAnnotation(javax.xml.ws.ResponseWrapper.class);
    if ((annotation != null) && (annotation.localName() != null) && (!"".equals(annotation.localName()))) {
      name = annotation.localName();
    }

    return name;
  }

  /**
   * The target namespace for the output.
   *
   * @return The target namespace for the output.
   */
  public String getTargetNamespace() {
    String targetNamespace = webMethod.getDeclaringEndpointInterface().getTargetNamespace();

    javax.xml.ws.ResponseWrapper annotation = webMethod.getAnnotation(javax.xml.ws.ResponseWrapper.class);
    if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
      targetNamespace = annotation.targetNamespace();
    }

    return targetNamespace;
  }

  /**
   * There's only one part to a doc/lit response wrapper.
   *
   * @return this.
   */
  public Collection<WebMessagePart> getParts() {
    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * @return false
   */
  public boolean isSimple() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isComplex() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isInput() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isOutput() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isHeader() {
    return false;
  }

  /**
   * @return false
   */
  public boolean isFault() {
    return false;
  }

  /**
   * The simple name of the method appended with "Response".
   *
   * @return The simple name of the method appended with "Response".
   */
  public String getMessageName() {
    return webMethod.getSimpleName() + "Response";
  }

  /**
   * The simple name of the method appended with "Response".
   *
   * @return The simple name of the method appended with "Response".
   */
  public String getPartName() {
    return webMethod.getSimpleName() + "Response";
  }

}
