package net.sf.enunciate.decorations.jaxws;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.VoidType;
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
public class WebMethod extends DecoratedMethodDeclaration {

  private final javax.jws.WebMethod annotation;
  private final Oneway oneWayAnnotation;
  private final WebService webService;

  public WebMethod(MethodDeclaration delegate) {
    super(delegate);

    if (!isWebMethod(delegate)) {
      throw new IllegalArgumentException(delegate + " isn't a web method.");
    }

    annotation = getAnnotation(javax.jws.WebMethod.class);
    oneWayAnnotation = getAnnotation(Oneway.class);
    webService = new WebService(getDeclaringType());
    validate();
  }

  /**
   * Whether the specified method is a web method.
   *
   * @param method The method to consider as a web method.
   * @return Whether the specified method is a web method.
   */
  public static boolean isWebMethod(MethodDeclaration method) {
    boolean isWebMethod = method.getModifiers().contains(Modifier.PUBLIC);
    javax.jws.WebMethod annotation = method.getAnnotation(javax.jws.WebMethod.class);
    if (annotation != null) {
      isWebMethod &= !annotation.exclude();
    }
    isWebMethod &= WebService.isWebService(method.getDeclaringType());
    return isWebMethod;
  }

  /**
   * Validates this web method.
   */
  protected void validate() {
    if (oneWayAnnotation != null) {
      if (!(getReturnType() instanceof VoidType)) {
        throw new IllegalStateException(getPosition() + ": a method cannot be one-way if it doesn't return void.");
      }

      if (getThrownTypes().size() > 0) {
        throw new IllegalStateException(getPosition() + ": a method cannot be one-way if it throws any exceptions.");
      }
    }

    SOAPBinding.ParameterStyle parameterStyle = getSoapParameterStyle();
    if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
      //make sure the conditions of a BARE web method are met according to the spec, section 3.6.2.2
      if (getSoapBindingStyle() != SOAPBinding.Style.DOCUMENT) {
        throw new IllegalStateException(String.format("%s: a %s-style web method cannot have a parameter style of %s",
                                                      getPosition(), getSoapBindingStyle(), parameterStyle));
      }

      int inParams = 0;
      int outParams = 0;
      for (WebParam webParam : getWebParameters()) {
        if (!webParam.isHeader()) {
          javax.jws.WebParam.Mode mode = webParam.getMode();

          if ((mode == javax.jws.WebParam.Mode.IN) || (mode == javax.jws.WebParam.Mode.INOUT)) {
            inParams++;
          }

          if ((mode == javax.jws.WebParam.Mode.OUT) || (mode == javax.jws.WebParam.Mode.INOUT)) {
            outParams++;
          }
        }
      }

      if (inParams > 1) {
        throw new IllegalStateException(getPosition() + ": a BARE web method must have at most 1 in or in/out non-header parameter.");
      }

      if (getReturnType() instanceof VoidType) {
        if (outParams > 1) {
          throw new IllegalStateException(getPosition() + ": a BARE web method that returns void must have at most 1 out or in/out non-header parameter.");
        }
      }
      else {
        if (outParams > 0) {
          throw new IllegalStateException(getPosition() + ": a BARE web method that doesn't return void must have no out or in/out parameters.");
        }
      }
    }
  }

  /**
   * Whether this web method is excluded.
   *
   * @return Whether this web method is excluded.
   */
  public boolean isExcluded() {
    return false;
  }

  /**
   * The web result of this web method.
   *
   * @return The web result of this web method.
   */
  public WebResult getWebResult() {
    return new WebResult(getReturnType(), this);
  }

  /**
   * The list of web parameters for this wethod.
   *
   * @return The list of web parameters for this wethod.
   */
  public Collection<WebParam> getWebParameters() {
    Collection<ParameterDeclaration> parameters = getParameters();
    Collection<WebParam> webParameters = new ArrayList<WebParam>(parameters.size());
    for (ParameterDeclaration parameter : parameters) {
      webParameters.add(new WebParam(parameter, this));
    }
    return webParameters;
  }

  /**
   * The list of web faults thrown by this method.
   *
   * @return The list of web faults thrown by this method.
   */
  public Collection<WebFault> getWebFaults() {
    Collection<WebFault> webFaults = new ArrayList<WebFault>();
    for (ReferenceType referenceType : getThrownTypes()) {
      if (!(referenceType instanceof DeclaredType)) {
        throw new IllegalStateException("How can a thrown type be anything other than a declared type?");
      }

      TypeDeclaration declaration = ((DeclaredType) referenceType).getDeclaration();

      if (declaration == null) {
        throw new IllegalStateException("Unknown declaration for " + referenceType);
      }

      webFaults.add(new WebFault(declaration));
    }

    return webFaults;
  }

  /**
   * The messages of this web method.
   *
   * @return The messages of this web method.
   */
  public Collection<WebMessage> getMessages() {
    Collection<WebMessage> messages = new ArrayList<WebMessage>();
    SOAPBinding.Style bindingStyle = getSoapBindingStyle();

    if (bindingStyle == SOAPBinding.Style.DOCUMENT) {
      SOAPBinding.ParameterStyle parameterStyle = getSoapParameterStyle();
      Collection<WebParam> webParams = getWebParameters();
      for (WebParam webParam : webParams) {
        switch(parameterStyle) {
          //add all the headers, and if it's BARE, add the (should be only one) parameter bare (not wrapped).
          case WRAPPED:
            if (!webParam.isHeader()) {
              break;
            }
          case BARE:
            messages.add(webParam);
            break;
          default:
            throw new UnsupportedOperationException(getPosition() + ": Unknown parameter style: " + parameterStyle);
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
      throw new UnsupportedOperationException(getPosition() + ": Sorry, " + bindingStyle + "-style methods aren't supported yet.");
    }

    return messages;
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
      if (message.isComplex()) {
        for (WebMessagePart part : ((ComplexWebMessage) message).getParts()) {
          namespaces.add(part.getTargetNamespace());
        }
      }
      else {
        namespaces.add(((SimpleWebMessage) message).getTargetNamespace());
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
    return (oneWayAnnotation != null);
  }

  /**
   * The SOAP binding style of this web method.
   *
   * @return The SOAP binding style of this web method.
   */
  public SOAPBinding.Style getSoapBindingStyle() {
    SOAPBinding.Style style = getDeclaringWebService().getSoapBindingStyle();
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.style();
    }

    if (style != SOAPBinding.Style.DOCUMENT) {
      //todo: support rpc-style web services.
      throw new UnsupportedOperationException(getPosition() + ": Sorry, " + style + "-style web methods aren't supported yet.");
    }

    return style;
  }

  /**
   * The declaring web service for this web method.
   *
   * @return The declaring web service for this web method.
   */
  public WebService getDeclaringWebService() {
    return webService;
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

}
