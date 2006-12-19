package net.sf.enunciate.modules.xml;

import net.sf.enunciate.contract.jaxb.Attribute;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.Element;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.xml.bind.annotation.XmlNsForm;
import java.util.HashMap;

/**
 * Validator for the xml module.
 *
 * @author Ryan Heaton
 */
public class XMLValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    HashMap<String, WebMethod> implicitElementNames = new HashMap<String, WebMethod>();
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

        for (WebMessagePart webMessagePart : webMessage.getParts()) {
          if (!(webMessagePart instanceof WebFault) && (webMessagePart.isImplicitSchemaElement())) {
            ImplicitSchemaElement el = ((ImplicitSchemaElement) webMessagePart);
            WebMethod otherMethod = implicitElementNames.put(el.getElementName(), webMethod);
            if (otherMethod != null) {
              result.addError(webMethod.getPosition(), "Web method defines a message part named '" + el.getElementName() +
                "' that is identical to the name of a web message part defined in " + otherMethod.getPosition() + ".  Please use annotations to disambiguate.");
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

    String typeNamespace = complexType.getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    boolean elementFormDefaultQualified = complexType.getSchema().getElementFormDefault() == XmlNsForm.QUALIFIED;

    for (Element element : complexType.getElements()) {
      if (element.getRef() == null) {
        String elementNamespace = element.getNamespace();
        elementNamespace = elementNamespace == null ? "" : elementNamespace;

        if ((elementFormDefaultQualified) && (!elementNamespace.equals(typeNamespace))) {
          result.addError(element.getPosition(), "Enunciate doesn't support elements of different namespaces than their type definitions.  The namespace ["
            + elementNamespace + "] of element [" + element.getName() + "] has a different namespace than [" + typeNamespace + "] of definition [" +
            element.getTypeDefinition().getName() + "].  You'll need to make this element an element ref.");
        }

        if ((!elementFormDefaultQualified) && (!"".equals(elementNamespace))) {
          result.addError(element.getPosition(), "Enunciate requires that if the elementFormDefault is not \"qualified\" the namespace of the element must " +
            "be the default namespace.");
        }
      }

      if (element.isWrapped()) {
        String wrapperNamespace = element.getWrapperNamespace();
        wrapperNamespace = wrapperNamespace == null ? "" : wrapperNamespace;

        if ((elementFormDefaultQualified) && (!wrapperNamespace.equals(typeNamespace))) {
          result.addError(element.getPosition(), "Enunciate doesn't support wrapper elements of different namespaces than their type definitions.  The wrapper namespace ["
            + wrapperNamespace + "] of element [" + element.getName() + "] has a different namespace than [" + typeNamespace + "] of definition [" +
            element.getTypeDefinition().getName() + "].");
        }

        if ((!elementFormDefaultQualified) && (!"".equals(wrapperNamespace))) {
          result.addError(element.getPosition(), "Enunciate requires that if the elementFormDefault is not \"qualified\" the namespace of the wrapper element must " +
            "be the default namespace.");
        }
      }
    }

    for (Attribute attribute : complexType.getAttributes()) {
      if (attribute.getRef() != null) {
        result.addError(attribute.getPosition(), "Enunciate doesn't support attribute refs.  If attributeFormDefault is \"qualified,\" the namespace of the " +
          "attribute must be the same as its containing type definition.  If attributeFormDefault is not \"qualified, \" the namespace of the attribute must " +
          "be the default namespace.");
      }
    }

    return result;
  }
}
