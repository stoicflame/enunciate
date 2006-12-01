package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.contract.jaxb.Accessor;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import java.util.ArrayList;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class XFireClientValidator extends BaseValidator {

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
    ArrayList<Accessor> accessors = new ArrayList<Accessor>();
    accessors.addAll(typeDef.getAttributes());
    accessors.add(typeDef.getValue());
    accessors.addAll(typeDef.getElements());
    for (Accessor accessor : accessors) {
      if ((accessor != null) && (accessor.getAnnotation(XmlIDREF.class) != null) && (accessor.getAnnotation(XmlList.class) != null)) {
        result.addError(accessor.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
      }
    }
  }

}
