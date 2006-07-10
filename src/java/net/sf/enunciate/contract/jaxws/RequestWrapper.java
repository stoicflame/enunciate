package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.util.QName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A request wrapper for a web method in document/literal wrapped style.
 *
 * @author Ryan Heaton
 */
public class RequestWrapper implements WebMessage, WebMessagePart, ImplicitRootElement {

  private WebMethod webMethod;

  /**
   * @param webMethod The web method to wrap.
   */
  protected RequestWrapper(WebMethod webMethod) {
    this.webMethod = webMethod;
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
  public String getElementName() {
    String name = webMethod.getSimpleName();

    javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
    if ((annotation != null) && (annotation.localName() != null) && (!"".equals(annotation.localName()))) {
      name = annotation.localName();
    }

    return name;
  }

  /**
   * @return true
   */
  public boolean isImplicitSchemaElement() {
    return true;
  }

  /**
   * The qname of the element for this request wrapper.
   *
   * @return The qname of the element for this request wrapper.
   */
  public QName getElementQName() {
    return new QName(webMethod.getDeclaringEndpointInterface().getTargetNamespace(), getElementName());
  }

  /**
   * The schema type of a request wrapper is always anonymous.
   *
   * @return null
   */
  public QName getTypeQName() {
    return null;
  }

  /**
   * The web parameters for the method that this is wrapping.
   *
   * @return The web parameters for the method that this is wrapping.
   */
  public Collection<ImplicitChildElement> getChildElements() {
    Collection<ImplicitChildElement> childElements = new ArrayList<ImplicitChildElement>();
    for (WebParam webParam : webMethod.getWebParameters()) {
      if (webParam.isInput() && !webParam.isHeader()) {
        childElements.add(webParam);
      }
    }
    return childElements;
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
    //todo: support rpc encoding.
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
