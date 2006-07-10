package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.util.QName;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A response wrapper for a web method in document/literal wrapped style.
 *
 * @author Ryan Heaton
 */
public class ResponseWrapper implements WebMessage, WebMessagePart, ImplicitRootElement {

  private WebMethod webMethod;

  /**
   * @param webMethod The web method to wrap.
   */
  protected ResponseWrapper(WebMethod webMethod) {
    this.webMethod = webMethod;
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
  public String getElementName() {
    String name = webMethod.getSimpleName() + "Response";

    javax.xml.ws.ResponseWrapper annotation = webMethod.getAnnotation(javax.xml.ws.ResponseWrapper.class);
    if ((annotation != null) && (annotation.localName() != null) && (!"".equals(annotation.localName()))) {
      name = annotation.localName();
    }

    return name;
  }

  /**
   * The qname of the response element.
   *
   * @return The qname of the response element.
   */
  public QName getElementQName() {
    return new QName(webMethod.getDeclaringEndpointInterface().getTargetNamespace(), getElementName());
  }

  /**
   * @return true.
   */
  public boolean isImplicitSchemaElement() {
    return true;
  }

  /**
   * The schema type for a response wrapper is always anonymous.
   *
   * @return null
   */
  public QName getTypeQName() {
    return null;
  }

  /**
   * The collection of output parameters for this response.
   *
   * @return The collection of output parameters for this response.
   */
  public Collection<ImplicitChildElement> getChildElements() {
    Collection<ImplicitChildElement> childElements = new ArrayList<ImplicitChildElement>();

    EnunciateFreemarkerModel model = ((EnunciateFreemarkerModel) FreemarkerModel.get());
    try {
      DecoratedTypeMirror returnType = (DecoratedTypeMirror) webMethod.getReturnType();
      if (!returnType.isVoid()) {
        XmlTypeMirror xmlType = model.getXmlType(returnType);
        if (xmlType.isAnonymous()) {
          throw new ValidationException(webMethod.getPosition(), "Return value must not be an anonymous type.");
        }
        int minOccurs = returnType.isPrimitive() ? 1 : 0;
        String maxOccurs = returnType.isArray() || returnType.isCollection() ? "unbounded" : "1";
        childElements.add(new ReturnChildElement(xmlType, minOccurs, maxOccurs));
      }
    }
    catch (XmlTypeException e) {
      throw new ValidationException(webMethod.getPosition(), e.getMessage());
    }

    for (WebParam webParam : webMethod.getWebParameters()) {
      if (webParam.isOutput() && !webParam.isHeader()) {
        childElements.add(webParam);
      }
    }

    return childElements;
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

  private static class ReturnChildElement implements ImplicitChildElement {

    private final XmlTypeMirror xmlType;
    private final int minOccurs;
    private final String maxOccurs;

    public ReturnChildElement(XmlTypeMirror xmlType, int minOccurs, String maxOccurs) {
      this.xmlType = xmlType;
      this.minOccurs = minOccurs;
      this.maxOccurs = maxOccurs;
    }

    public String getElementName() {
      return "return";
    }

    public QName getTypeQName() {
      return xmlType.getQname();
    }

    public int getMinOccurs() {
      return minOccurs;
    }

    public String getMaxOccurs() {
      return maxOccurs;
    }

  }

}
