package net.sf.enunciate.contract.jaxws;

import javax.xml.namespace.QName;
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
   * The name of the JAXWS request bean.
   *
   * @return The name of the JAXWS request bean.
   */
  public String getRequestBeanName() {
    String capitalizedName = this.webMethod.getSimpleName();
    capitalizedName = Character.toString(capitalizedName.charAt(0)).toUpperCase() + capitalizedName.substring(1);
    String requestBeanName = this.webMethod.getDeclaringEndpointInterface().getPackage().getQualifiedName() + ".jaxws." + capitalizedName;

    javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
    if ((annotation != null) && (annotation.className() != null) && (!"".equals(annotation.className()))) {
      requestBeanName = annotation.className();
    }

    return requestBeanName;
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

  public String getElementNamespace() {
    String targetNamespace = webMethod.getDeclaringEndpointInterface().getTargetNamespace();

    javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
    if ((annotation != null) && (annotation.localName() != null) && (!"".equals(annotation.targetNamespace()))) {
      targetNamespace = annotation.targetNamespace();
    }

    return targetNamespace;
  }

  /**
   * Documentation explaining this is a request wrapper for its method.
   *
   * @return Documentation explaining this is a request wrapper for its method.
   */
  public String getElementDocs() {
    String docs = "doc/lit request wrapper for operation \"" + webMethod.getOperationName() + "\".";
    String methodDocs = webMethod.getJavaDoc().toString();
    if (methodDocs.trim().length() > 0) {
      docs += " (" + methodDocs.trim() + ")";
    }
    return docs;
  }

  /**
   * @return true
   */
  public boolean isImplicitSchemaElement() {
    return true;
  }

  /**
   * @return The enum {@link ParticleType#ELEMENT}
   */
  public ParticleType getParticleType() {
    return ParticleType.ELEMENT;
  }

  /**
   * The qname of the element for this request wrapper.
   *
   * @return The qname of the element for this request wrapper.
   */
  public QName getParticleQName() {
    return new QName(getElementNamespace(), getElementName());
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
    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * The simple name of the method.
   *
   * @return The simple name of the method.
   */
  public String getMessageName() {
    return webMethod.getDeclaringEndpointInterface().getSimpleName() + "." + webMethod.getSimpleName();
  }

  /**
   * Documentation explaining this is a request message for its method.
   *
   * @return Documentation explaining this is a request message for its method.
   */
  public String getMessageDocs() {
    String docs = "request message for operation \"" + webMethod.getOperationName() + "\".";
    String methodDocs = webMethod.getJavaDoc().toString();
    if (methodDocs.trim().length() > 0) {
      docs += " (" + methodDocs.trim() + ")";
    }
    return docs;
  }

  /**
   * @return null
   */
  public String getPartDocs() {
    return null;
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
