package net.sf.enunciate.contract.jaxws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A request wrapper for a web method in document/literal wrapped style.
 *
 * @author Ryan Heaton
 */
public class RequestWrapper implements ComplexWebMessage, WebMessagePart {

  private WebMethod webMethod;

  /**
   * @param webMethod The web method to wrap.
   */
  public RequestWrapper(WebMethod webMethod) {
    this.webMethod = webMethod;

    webMethod.getDeclaringEndpointInterface().getValidator().validate(this);
  }

  /**
   * The web method associated with this request wrapper.
   *
   * @return The web method associated with this request wrapper.
   */
  public WebMethod getWebMethod() {
    return webMethod;
  }

  /**
   * The local name of the element.
   *
   * @return The local name of the element.
   */
  public String getName() {
    String name = webMethod.getSimpleName();

    javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
    if ((annotation != null) && (annotation.localName() != null) && (!"".equals(annotation.localName()))) {
      name = annotation.localName();
    }

    return name;
  }

  /**
   * The target namespace for the input.
   *
   * @return The target namespace for the input.
   */
  public String getTargetNamespace() {
    String targetNamespace = webMethod.getDeclaringEndpointInterface().getTargetNamespace();

    javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
    if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
      targetNamespace = annotation.targetNamespace();
    }

    return targetNamespace;
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
   * @return true
   */
  public boolean isInput() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isOutput() {
    return false;
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
   * There's only one part to a doc/lit request wrapper.
   *
   * @return this.
   */
  public Collection<WebMessagePart> getParts() {
    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * The simple name of the method.
   *
   * @return The simple name of the method.
   */
  public String getMessageName() {
    return webMethod.getSimpleName();
  }

  /**
   * The simple name of the method.
   *
   * @return The simple name of the method.
   */
  public String getPartName() {
    return webMethod.getSimpleName();
  }

}
