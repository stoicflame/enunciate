package net.sf.enunciate.contract.jaxb.validation;

import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.Modifier;
import net.sf.enunciate.contract.ValidationResult;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.GlobalElementDeclaration;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxb.TypeDefinition;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Default implementation of the JAXB validator.
 *
 * @author Ryan Heaton
 */
public class DefaultJAXBValidator implements JAXBValidator {

  // Inherited.
  public ValidationResult validate(ComplexTypeDefinition complexType) {
    ValidationResult result = validate((TypeDefinition) complexType);

    //todo: validate that no superclasses are simple type definitions.

    return result;
  }

  // Inherited.
  public ValidationResult validate(SimpleTypeDefinition simpleType) {
    ValidationResult result = validate((TypeDefinition) simpleType);

    //todo: validate that all of its members (including members of superclass) are mapped to an attribute.

    return result;
  }

  /**
   * Validation logic common to all type definitions.
   *
   * @param typeDef The type definition to validate.
   */
  public ValidationResult validate(TypeDefinition typeDef) {
    ValidationResult result = new ValidationResult();

    if (isXmlTransient(typeDef)) {
      result.getErrors().add(typeDef.getPosition() + ": A XmlTransient declaration shouldn't be valid.");
    }

    XmlType xmlType = typeDef.getAnnotation(XmlType.class);

    boolean needsNoArgConstructor = true;
    if (xmlType != null) {
      if ((typeDef.getDeclaringType() != null) && (!typeDef.getModifiers().contains(Modifier.STATIC))) {
        result.getErrors().add("A xml type must be either a top-level class or a nested static class.");
      }

      Class factoryClass = xmlType.factoryClass();
      String factoryMethod = xmlType.factoryMethod();

      if ((factoryClass != XmlType.DEFAULT.class) || (!"".equals(factoryMethod))) {
        needsNoArgConstructor = false;
        try {
          Method method = factoryClass.getMethod(factoryMethod, null);
          if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            //todo: is this really a requirement?
            result.getErrors().add(typeDef.getPosition() + ": '" + factoryMethod + "' must be a static, no-arg method on '" + factoryClass.getName() + "'.");
          }
        }
        catch (NoSuchMethodException e) {
          result.getErrors().add(typeDef.getPosition() + ": Unknown factory method '" + factoryMethod + "' on class '" + factoryClass.getName() + "'.");
        }
      }
      else if (typeDef.getAnnotation(XmlJavaTypeAdapter.class) != null) {
        needsNoArgConstructor = false;
        //todo: validate that this is a valid type adapter?
      }

      String[] propOrder = xmlType.propOrder();
      if ((propOrder.length > 0) && (!"".equals(propOrder[0]))) {
        //todo: validate that all properties and fields are accounted for in the propOrder list.
      }
    }

    if (needsNoArgConstructor) {
      //check for a zero-arg constructor...
      boolean hasNoArgConstructor = false;
      Collection<ConstructorDeclaration> constructors = typeDef.getConstructors();
      for (ConstructorDeclaration constructor : constructors) {
        if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().size() == 0)) {
          hasNoArgConstructor = true;
          break;
        }
      }

      if (!hasNoArgConstructor) {
        result.getErrors().add(typeDef.getPosition() + ": an TypeDefinition must have a public no-arg constructor or be annotated with a factory method.");
      }
    }

    return result;
  }

// Inherited.

  public ValidationResult validate(GlobalElementDeclaration globalElementDeclaration) {
    return null;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(Declaration declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
  }
}
