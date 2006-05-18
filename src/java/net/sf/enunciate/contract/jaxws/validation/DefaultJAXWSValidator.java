package net.sf.enunciate.contract.jaxws.validation;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.VoidType;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

/**
 * Default JAX-WS validator.
 *
 * @author Ryan Heaton
 */
public class DefaultJAXWSValidator implements JAXWSValidator {

  //Inherited.
  public boolean isEndpointImplementation(TypeDeclaration declaration) {
    boolean is = false;
    WebService ws = declaration.getAnnotation(WebService.class);
    if (ws != null) {
      is = (declaration instanceof ClassDeclaration) && !(declaration instanceof EnumDeclaration);
    }
    return is;
  }

  //Inherited.
  public boolean isEndpointInterface(TypeDeclaration declaration) {
    boolean is = false;
    WebService ws = declaration.getAnnotation(WebService.class);
    if (ws != null) {
      is = (declaration instanceof InterfaceDeclaration)
        //if this is a class declaration, then it has an implicit endpoint interface if it doesn't reference another.
        || ((ws.endpointInterface() != null) && (!"".equals(ws.endpointInterface())));
    }
    return is;
  }

  // Inherited.
  public boolean isWebMethod(MethodDeclaration method) {
    boolean isWebMethod = method.getModifiers().contains(Modifier.PUBLIC);
    javax.jws.WebMethod annotation = method.getAnnotation(javax.jws.WebMethod.class);
    if (annotation != null) {
      isWebMethod &= !annotation.exclude();
    }
    isWebMethod &= isEndpointInterface(method.getDeclaringType());
    return isWebMethod;
  }

  //Inherited.
  public void validate(EndpointImplementation impl) throws ValidationException {
    Declaration delegate = impl.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      throw new ValidationException(delegate + " is not an endpoint implementation!");
    }

    if (delegate instanceof InterfaceDeclaration) {
      throw new ValidationException(impl.getAnnotations().get(javax.jws.WebService.class.getName()).getPosition() +
        ": an interface type declaration cannot be an endpoint implementation (the specified endpoint interface is not empty).");
    }

    if (delegate instanceof EnumDeclaration) {
      throw new ValidationException(delegate.getPosition() + ": enums cannot be endpoint implementations.");
    }

  }

  //Inherited.
  public void validate(EndpointInterface ei) throws ValidationException {
    Declaration delegate = ei.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      throw new ValidationException(delegate + " is not an endpoint interface!");
    }
    else if ((ei.getPackage() == null) && (ei.getTargetNamespace() == null)) {
      throw new ValidationException(delegate.getPosition() + ": An endpoint interface in no package must specify a target namespace.");
    }

    if (delegate instanceof AnnotationTypeDeclaration) {
      throw new ValidationException(delegate.getPosition() + ": annotation types cannot be endpoint interfaces.");
    }

    if (delegate instanceof EnumDeclaration) {
      throw new ValidationException(delegate.getPosition() + ": enums cannot be endpoint interfaces.");
    }

    if ((ws.endpointInterface() != null) & (!"".equals(ws.endpointInterface()))) {
      throw new ValidationException(delegate + " is not an endpoint interface (it references another endpoint interface).");
    }
  }

  // Inherited.
  public void validate(WebMethod webMethod) throws ValidationException {
    if (!webMethod.getModifiers().contains(Modifier.PUBLIC)) {
      throw new ValidationException("A non-public method cannot be a web method.");
    }

    javax.jws.WebMethod annotation = webMethod.getAnnotation(javax.jws.WebMethod.class);
    if ((annotation != null) && (annotation.exclude())) {
      throw new ValidationException("A method marked as excluded cannot be a web method.");
    }

    if (webMethod.isOneWay()) {
      if (!(webMethod.getReturnType() instanceof VoidType)) {
        throw new ValidationException(webMethod.getPosition() + ": a method cannot be one-way if it doesn't return void.");
      }

      if (webMethod.getThrownTypes().size() > 0) {
        throw new ValidationException(webMethod.getPosition() + ": a method cannot be one-way if it throws any exceptions.");
      }
    }

    SOAPBinding.ParameterStyle parameterStyle = webMethod.getSoapParameterStyle();
    if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
      //make sure the conditions of a BARE web method are met according to the spec, section 3.6.2.2
      if (webMethod.getSoapBindingStyle() != SOAPBinding.Style.DOCUMENT) {
        throw new ValidationException(String.format("%s: a %s-style web method cannot have a parameter style of %s",
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
        throw new ValidationException(webMethod.getPosition() + ": a BARE web method must have at most 1 in or in/out non-header parameter.");
      }

      if (webMethod.getReturnType() instanceof VoidType) {
        if (outParams > 1) {
          throw new ValidationException(webMethod.getPosition() + ": a BARE web method that returns void must have at most 1 out or in/out non-header parameter.");
        }
      }
      else {
        if (outParams > 0) {
          throw new ValidationException(webMethod.getPosition() + ": a BARE web method that doesn't return void must have no out or in/out parameters.");
        }
      }
    }
  }

  // Inherited.
  public void validate(RequestWrapper requestWrapper) throws ValidationException {
    if (requestWrapper.getWebMethod().getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
      throw new ValidationException("A BARE web method shouldn't have a request wrapper.");
    }
  }

  // Inherited.
  public void validate(ResponseWrapper responseWrapper) throws ValidationException {
    if (responseWrapper.getWebMethod().getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
      throw new ValidationException("A BARE web method shouldn't have a response wrapper.");
    }

    if (responseWrapper.getWebMethod().isOneWay()) {
      throw new ValidationException("A one-way method cannot have a response wrapper.");
    }
  }

  // Inherited.
  public void validate(WebParam webParam) throws ValidationException {
    DecoratedTypeMirror parameterType = (DecoratedTypeMirror) webParam.getType();
    if (parameterType.isInstanceOf(Holder.class.getName())) {
      throw new UnsupportedOperationException(webParam.getPosition() + ": enunciate currently doesn't support in/out parameters.  Maybe someday...");
    }
  }

  // Inherited.
  public void validate(WebResult webResult) throws ValidationException {
    //nothing to validate?
  }

  // Inherited.
  public void validate(WebFault webFault) throws ValidationException {
    //nothing to validate?
  }

}
