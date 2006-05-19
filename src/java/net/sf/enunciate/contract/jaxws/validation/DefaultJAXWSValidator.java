package net.sf.enunciate.contract.jaxws.validation;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.VoidType;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Default JAX-WS validator.
 *
 * @author Ryan Heaton
 */
public class DefaultJAXWSValidator implements JAXWSValidator {

  //Inherited.
  public ValidationResult validate(EndpointImplementation impl) throws ValidationException {
    ValidationResult result = new ValidationResult();
    Declaration delegate = impl.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.getErrors().add(delegate + " is not an endpoint implementation!");
    }

    if (delegate instanceof EnumDeclaration) {
      result.getErrors().add(delegate.getPosition() + ": enums cannot be endpoint implementations.");
    }

    if (!isAssignable((TypeDeclaration) delegate, (InterfaceDeclaration) impl.getEndpointInterface().getDelegate())) {
      result.getErrors().add(delegate.getPosition() + ": class does not implement its endpoint interface!");
    }

    return result;
  }

  /**
   * Whether declaration1 is assignable to declaration2.
   *
   * @return Whether declaration1 is assignable to declaration2.
   */
  protected boolean isAssignable(TypeDeclaration declaration1, InterfaceDeclaration declaration2) {
    String iffqn = declaration2.getQualifiedName();
    if (declaration1.getQualifiedName().equals(iffqn)) {
      return true;
    }

    Collection<InterfaceType> superinterfaces = declaration1.getSuperinterfaces();
    for (InterfaceType interfaceType : superinterfaces) {
      InterfaceDeclaration declaration = interfaceType.getDeclaration();
      if ((declaration != null) && (isAssignable(declaration, declaration2))) {
        return true;
      }
    }

    return false;
  }

  //Inherited.
  public ValidationResult validate(EndpointInterface ei) throws ValidationException {
    ValidationResult result = new ValidationResult();

    Declaration delegate = ei.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.getErrors().add(delegate + " is not an endpoint interface!");
    }
    else {
      if ((ei.getPackage() == null) && (ei.getTargetNamespace() == null)) {
        result.getErrors().add(delegate.getPosition() + ": An endpoint interface in no package must specify a target namespace.");
      }

      if ((ws.endpointInterface() != null) & (!"".equals(ws.endpointInterface()))) {
        result.getErrors().add(delegate + " is not an endpoint interface (it references another endpoint interface).");
      }
    }

    if (delegate instanceof AnnotationTypeDeclaration) {
      result.getErrors().add(delegate.getPosition() + ": annotation types cannot be endpoint interfaces.");
    }

    if (delegate instanceof EnumDeclaration) {
      result.getErrors().add(delegate.getPosition() + ": enums cannot be endpoint interfaces.");
    }

    TreeSet<WebMethod> uniquelyNamedWebMethods = new TreeSet<WebMethod>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (!uniquelyNamedWebMethods.add(webMethod)) {
        result.getErrors().add(webMethod.getPosition() + ": web methods must have unique operation names.  Use annotations to disambiguate.");
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validate(WebMethod webMethod) throws ValidationException {
    ValidationResult result = new ValidationResult();
    if (!webMethod.getModifiers().contains(Modifier.PUBLIC)) {
      result.getErrors().add("A non-public method cannot be a web method.");
    }

    javax.jws.WebMethod annotation = webMethod.getAnnotation(javax.jws.WebMethod.class);
    if ((annotation != null) && (annotation.exclude())) {
      result.getErrors().add("A method marked as excluded cannot be a web method.");
    }

    if (webMethod.isOneWay()) {
      if (!(webMethod.getReturnType() instanceof VoidType)) {
        result.getErrors().add(webMethod.getPosition() + ": a method cannot be one-way if it doesn't return void.");
      }

      if (webMethod.getThrownTypes().size() > 0) {
        result.getErrors().add(webMethod.getPosition() + ": a method cannot be one-way if it throws any exceptions.");
      }
    }

    SOAPBinding.ParameterStyle parameterStyle = webMethod.getSoapParameterStyle();
    if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
      //make sure the conditions of a BARE web method are met according to the spec, section 3.6.2.2
      if (webMethod.getSoapBindingStyle() != SOAPBinding.Style.DOCUMENT) {
        result.getErrors().add(String.format("%s: a %s-style web method cannot have a parameter style of %s",
                                             webMethod.getPosition(), webMethod.getSoapBindingStyle(), parameterStyle));
      }

      int inParams = 0;
      int outParams = 0;
      for (WebParam webParam : webMethod.getWebParameters()) {
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
        result.getErrors().add(webMethod.getPosition() + ": a BARE web method must have at most 1 in or in/out non-header parameter.");
      }

      if (webMethod.getReturnType() instanceof VoidType) {
        if (outParams > 1) {
          result.getErrors().add(webMethod.getPosition() + ": a BARE web method that returns void must have at most 1 out or in/out non-header parameter.");
        }
      }
      else {
        if (outParams > 0) {
          result.getErrors().add(webMethod.getPosition() + ": a BARE web method that doesn't return void must have no out or in/out parameters.");
        }
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validate(RequestWrapper requestWrapper) throws ValidationException {
    ValidationResult result = new ValidationResult();
    if (requestWrapper.getWebMethod().getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
      result.getErrors().add("A BARE web method shouldn't have a request wrapper.");
    }
    return result;
  }

  // Inherited.
  public ValidationResult validate(ResponseWrapper responseWrapper) throws ValidationException {
    ValidationResult result = new ValidationResult();
    if (responseWrapper.getWebMethod().getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
      result.getErrors().add("A BARE web method shouldn't have a response wrapper.");
    }

    if (responseWrapper.getWebMethod().isOneWay()) {
      result.getErrors().add("A one-way method cannot have a response wrapper.");
    }
    return result;
  }

  // Inherited.
  public ValidationResult validate(WebParam webParam) throws ValidationException {
    ValidationResult result = new ValidationResult();
    DecoratedTypeMirror parameterType = (DecoratedTypeMirror) webParam.getType();
    if (parameterType.isInstanceOf(Holder.class.getName())) {
      result.getErrors().add(webParam.getPosition() + ": enunciate currently doesn't support in/out parameters.  Maybe someday...");
    }
    return result;
  }

  // Inherited.
  public ValidationResult validate(WebResult webResult) throws ValidationException {
    return new ValidationResult();
  }

  // Inherited.
  public ValidationResult validate(WebFault webFault) throws ValidationException {
    return new ValidationResult();
  }

}
