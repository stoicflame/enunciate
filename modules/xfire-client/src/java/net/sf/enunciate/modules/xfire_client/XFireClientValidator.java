package net.sf.enunciate.modules.xfire_client;

import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.contract.jaxb.Accessor;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.contract.jaxws.WebParam;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class XFireClientValidator extends BaseValidator {


  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    HashSet<String> uniqueMethodNames = new HashSet<String>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (!uniqueMethodNames.add(webMethod.getSimpleName())) {
        result.addError(webMethod.getPosition(), "Sorry, the xfire client module doesn't support overloaded methods yet.  Unfortunately, each method has " +
          "to have a unique name.");
      }

      for (WebParam webParam : webMethod.getWebParameters()) {
        if (webParam.isOutput()) {
          //todo: add support for in in/out parameters.
          result.addError(webParam.getPosition(), "The xfire client module doesn't support IN/OUT or OUT parameters yet....");
        }
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
    ArrayList<Accessor> accessors = new ArrayList<Accessor>();
    accessors.addAll(typeDef.getAttributes());
    accessors.add(typeDef.getValue());
    accessors.addAll(typeDef.getElements());
    for (Accessor accessor : accessors) {
      if (accessor != null) {
        if ((accessor.getAnnotation(XmlIDREF.class) != null) && (accessor.getAnnotation(XmlList.class) != null)) {
          result.addError(accessor.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
        }

        TypeMirror accessorType = accessor.getBareAccessorType();
        if (accessorType instanceof DeclaredType) {
          String accessorTypeFQN = ((DeclaredType) accessorType).getDeclaration().getQualifiedName();
          if (java.awt.Image.class.getName().equals(accessorTypeFQN)) {
            result.addError(accessor.getPosition(), "xfire-client module doesn't yet support handling java.awt.Image.");
          }
          else if (javax.xml.transform.Source.class.getName().equals(accessorTypeFQN)) {
            result.addError(accessor.getPosition(), "xfire-client module doesn't yet support handling javax.xml.transform.Source.");
          }
        }
      }
    }
  }

}
