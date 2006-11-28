package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class XFireClientValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (webMethod.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
        result.addError(webMethod.getPosition(), "XFire clients don't support RPC-style web methods... yet....");
      }
    }
    return result;
  }


  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    validateTypeDefinition(complexType, result);
    return result;
  }

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    validateTypeDefinition(simpleType, result);
    return result;
  }

  public void validateTypeDefinition(TypeDefinition typeDef, ValidationResult result) {
    for (Attribute attribute : typeDef.getAttributes()) {
      if ((attribute.getAnnotation(XmlIDREF.class) != null) && (attribute.getAnnotation(XmlList.class) != null)) {
        result.addError(attribute.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
      }
    }

    Value value = typeDef.getValue();
    if ((value != null) && (value.getAnnotation(XmlIDREF.class) != null) && (value.getAnnotation(XmlList.class) != null)) {
      result.addError(value.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
    }

    for (Element element : typeDef.getElements()) {
      if ((element.getAnnotation(XmlIDREF.class) != null) && (element.getAnnotation(XmlList.class) != null)) {
        result.addError(element.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
      }
    }
  }

}
