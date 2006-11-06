package net.sf.enunciate.modules.xml;

import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.Element;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Validator for the xml module.
 *
 * @author Ryan Heaton
 */
public class XMLValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        if (webMessage instanceof RequestWrapper) {
          javax.xml.ws.RequestWrapper annotation = webMethod.getAnnotation(javax.xml.ws.RequestWrapper.class);
          if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
            if (!webMethod.getDeclaringEndpointInterface().getTargetNamespace().equals(annotation.targetNamespace())) {
              // the reason for this is that the wsdl we generate includes all request and response wrappers inline,
              // and the schemas we generate don't take into account request and response wrappers.
              result.addError(webMethod.getPosition(), "Enunciate doesn't allow declaring a target namespace for a request wrapper that is different " +
                "from the target namespace of the endpoint interface.  If you really must, declare the parameter style BARE and use an xml root element from " +
                "another namespace for the parameter.");
            }
          }
        }
        else if (webMessage instanceof ResponseWrapper) {
          javax.xml.ws.ResponseWrapper annotation = webMethod.getAnnotation(javax.xml.ws.ResponseWrapper.class);
          if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
            // the reason for this is that the wsdl we generate includes all request and response wrappers inline,
            // and the schemas we generate don't take into account request and response wrappers.
            if (!webMethod.getDeclaringEndpointInterface().getTargetNamespace().equals(annotation.targetNamespace())) {
              result.addError(webMethod.getPosition(), "Enunciate doesn't allow declaring a target namespace for a response wrapper that is " +
                "different from the target namespace of the endpoint interface.  If you really must, declare the parameter style BARE and use an xml root " +
                "element from another namespace for the return value.");
            }
          }
        }
      }

      for (WebParam webParam : webMethod.getWebParameters()) {
        javax.jws.WebParam annotation = webParam.getAnnotation(javax.jws.WebParam.class);
        if ((annotation != null) && (!"".equals(annotation.targetNamespace()))) {
          // the reason for this is that the wsdl we generate includes all parameter elements inline,
          // and the schemas we generate don't take into account the parameter elements.
          if (!annotation.targetNamespace().equals(webParam.getWebMethod().getDeclaringEndpointInterface().getTargetNamespace())) {
            result.addError(webParam.getPosition(), "Enunciate doesn't allow declaring a target namespace for a web parameter that is different from the " +
              "target namespace of the endpoint interface.  If you really want to, declare the parameter style BARE and use an xml root element from another " +
              "namespace for the parameter.");
          }
        }
      }

      if (!webMethod.getDeclaringEndpointInterface().getTargetNamespace().equals(webMethod.getWebResult().getTargetNamespace())) {
        // the reason for this is that the wsdl we generate includes all return elements inline,
        // and the schemas we generate don't take into account the return elements.
        result.addError(webMethod.getPosition(), "Enunciate doesn't allow methods to return a web result with a target namespace that is " +
          "declared different from the target namespace of its endpoint interface.  If you really want to, declare the parameter style BARE and use " +
          "an xml root element from another namespace for the return value.");
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);

    for (Element element : complexType.getElements()) {
      if (element.isWrapped()) {
        XmlElementWrapper wrapper = element.getAnnotation(XmlElementWrapper.class);

        String namespace = wrapper.namespace();
        String typeNamespace = element.getTypeDefinition().getNamespace();
        //use the empty string for comparison in the case of the empty namespace.
        if (namespace == null) {
          namespace = "";
        }
        if (typeNamespace == null) {
          typeNamespace = "";
        }

        if ((!"##default".equals(namespace)) && (!typeNamespace.equals(namespace))) {
          result.addError(element.getPosition(), "Enunciate doesn't support element wrappers of a different namespace than their containing type definition.  " +
            "The spec is unclear as to why this should be allowed because you could just use an @XmlElement annotation to accomplish the same thing with more clarity.");
        }
      }
    }

    return result;
  }
}
