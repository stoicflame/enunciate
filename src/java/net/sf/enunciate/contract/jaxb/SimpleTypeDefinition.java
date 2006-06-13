package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;

/**
 * A simple type definition.
 *
 * @author Ryan Heaton
 */
public class SimpleTypeDefinition extends TypeDefinition {

  public SimpleTypeDefinition(ClassDeclaration delegate, JAXBValidator validator) {
    super(delegate);
    validator.validate(this);
  }

  protected SimpleTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  /**
   * The base type for this simple type, or null if none exists.
   *
   * @return The base type for this simple type.
   */
  public TypeMirror getBaseType() {
    ValueAccessor value = getValue();

    if (value != null) {
      return value.getAccessorType();
    }

    return null;
  }

}
