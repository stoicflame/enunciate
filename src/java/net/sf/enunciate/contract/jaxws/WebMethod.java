package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ReferenceType;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.util.QName;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

import javax.jws.Oneway;
import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A method invoked on a web service.
 *
 * @author Ryan Heaton
 */
public class WebMethod extends DecoratedMethodDeclaration implements Comparable<WebMethod> {

  private final javax.jws.WebMethod annotation;
  private final boolean oneWay;
  private final EndpointInterface endpointInterface;
  private final WebResult webResult;
  private final Collection<WebParam> webParams;
  private final Collection<WebFault> webFaults;
  private final Collection<WebMessage> messages;

  protected WebMethod(MethodDeclaration delegate, EndpointInterface endpointInterface) {
    super(delegate);

    this.annotation = getAnnotation(javax.jws.WebMethod.class);
    this.oneWay = getAnnotation(Oneway.class) != null;
    this.endpointInterface = endpointInterface;
    this.webResult = new WebResult(getReturnType(), this);

    Collection<ParameterDeclaration> parameters = getParameters();
    Collection<WebParam> webParameters = new ArrayList<WebParam>(parameters.size());
    for (ParameterDeclaration parameter : parameters) {
      webParameters.add(new WebParam(parameter, this));
    }
    this.webParams = webParameters;

    Collection<WebFault> webFaults = new ArrayList<WebFault>();
    for (ReferenceType referenceType : getThrownTypes()) {
      if (!(referenceType instanceof DeclaredType)) {
        throw new ValidationException(getPosition(), "Thrown type must be a declared type.");
      }

      TypeDeclaration declaration = ((DeclaredType) referenceType).getDeclaration();

      if (declaration == null) {
        throw new ValidationException(getPosition(), "Unknown declaration for " + referenceType);
      }

      webFaults.add(new WebFault((ClassDeclaration) declaration, this));
    }
    this.webFaults = webFaults;

    Collection<WebMessage> messages = new ArrayList<WebMessage>();
    SOAPBinding.Style bindingStyle = getSoapBindingStyle();

    if (bindingStyle == SOAPBinding.Style.DOCUMENT) {
      SOAPBinding.ParameterStyle parameterStyle = getSoapParameterStyle();
      Collection<WebParam> webParams = getWebParameters();
      for (WebParam webParam : webParams) {
        switch (parameterStyle) {
          //add all the headers, and if it's BARE, add the (should be only one) parameter bare (not wrapped).
          case WRAPPED:
            if (!webParam.isHeader()) {
              break;
            }
          case BARE:
            messages.add(webParam);
            break;
        }
      }

      if (parameterStyle == SOAPBinding.ParameterStyle.WRAPPED) {
        messages.add(new RequestWrapper(this));
        if (!isOneWay()) {
          messages.add(new ResponseWrapper(this));
        }
      }

      if (!isOneWay()) {
        //add all the faults.
        messages.addAll(getWebFaults());
      }
    }
    else {
      //todo: support rpc-style operations.
      throw new ValidationException(getPosition(), "Sorry, " + bindingStyle + "-style methods aren't supported yet.");
    }

    this.messages = messages;
  }

  /**
   * The web result of this web method.
   *
   * @return The web result of this web method.
   */
  public WebResult getWebResult() {
    return this.webResult;
  }

  /**
   * The list of web parameters for this method.
   *
   * @return The list of web parameters for this method.
   */
  public Collection<WebParam> getWebParameters() {
    return this.webParams;
  }

  /**
   * The list of web faults thrown by this method.
   *
   * @return The list of web faults thrown by this method.
   */
  public Collection<WebFault> getWebFaults() {
    return this.webFaults;
  }

  /**
   * The messages of this web method.
   *
   * @return The messages of this web method.
   */
  public Collection<WebMessage> getMessages() {
    return this.messages;
  }

  /**
   * A set of the reference namespace for this method.
   *
   * @return A set of the reference namespace for this method.
   */
  public Set<String> getReferencedNamespaces() {
    TreeSet<String> namespaces = new TreeSet<String>();
    Collection<WebMessage> messages = getMessages();
    for (WebMessage message : messages) {
      for (WebMessagePart part : message.getParts()) {
        namespaces.add(part.getElementQName().getNamespaceURI(/**/));
        if (part instanceof ImplicitSchemaElement) {
          ImplicitSchemaElement implicitElement = (ImplicitSchemaElement) part;

          QName typeQName = implicitElement.getTypeQName();
          if (typeQName != null) {
            namespaces.add(typeQName.getNamespaceURI());
          }

          if (implicitElement instanceof ImplicitRootElement) {
            for (ImplicitChildElement childElement : ((ImplicitRootElement) implicitElement).getChildElements()) {
              namespaces.add(childElement.getTypeQName().getNamespaceURI());
            }
          }
        }
      }
    }
    return namespaces;
  }

  /**
   * The message name of this web method.
   *
   * @return The message name of this web method.
   */
  public String getMessageName() {
    return getSimpleName();
  }

  /**
   * The message name of the response of this web method.
   *
   * @return The message name of the response of this web method.
   */
  public String getResponseMessageName() {
    return getSimpleName() + "Response";
  }

  /**
   * The operation name of this web method.
   *
   * @return The operation name of this web method.
   */
  public String getOperationName() {
    String operationName = getSimpleName();

    if ((annotation != null) && (!"".equals(annotation.operationName()))) {
      return annotation.operationName();
    }

    return operationName;
  }

  /**
   * The action of this web method.
   *
   * @return The action of this web method.
   */
  public String getAction() {
    String action = "";

    if (annotation != null) {
      action = annotation.action();
    }

    return action;
  }

  /**
   * Whether this web method is one-way.
   *
   * @return Whether this web method is one-way.
   */
  public boolean isOneWay() {
    return oneWay;
  }

  /**
   * The SOAP binding style of this web method.
   *
   * @return The SOAP binding style of this web method.
   */
  public SOAPBinding.Style getSoapBindingStyle() {
    SOAPBinding.Style style = getDeclaringEndpointInterface().getSoapBindingStyle();
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.style();
    }

    if (style != SOAPBinding.Style.DOCUMENT) {
      //todo: support rpc-style web services.
      throw new ValidationException(getPosition(), "Sorry, " + style + "-style web methods aren't supported yet.");
    }

    return style;
  }

  /**
   * The declaring web service for this web method.
   *
   * @return The declaring web service for this web method.
   */
  public EndpointInterface getDeclaringEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The SOAP binding use of this web method.
   *
   * @return The SOAP binding use of this web method.
   */
  public SOAPBinding.Use getSoapUse() {
    SOAPBinding.Use use = SOAPBinding.Use.LITERAL;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      use = bindingInfo.use();
    }

    if (use != SOAPBinding.Use.LITERAL) {
      throw new ValidationException(getPosition(), use.toString().toLowerCase() + "-use web methods are not supported by enunciate");
    }

    return use;
  }

  /**
   * The SOAP parameter style of this web method.
   *
   * @return The SOAP parameter style of this web method.
   */
  public SOAPBinding.ParameterStyle getSoapParameterStyle() {
    SOAPBinding.ParameterStyle style = SOAPBinding.ParameterStyle.WRAPPED;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.parameterStyle();
    }

    return style;
  }

  /**
   * Web methods must be unique by name.  (JSR 181: 3.1.1)
   *
   * @param webMethod The web method to compare this to.
   * @return The comparison.
   */
  public int compareTo(WebMethod webMethod) {
    return getOperationName().compareTo(webMethod.getOperationName());
  }

}
