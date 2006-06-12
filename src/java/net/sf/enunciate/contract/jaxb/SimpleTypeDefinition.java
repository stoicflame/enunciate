package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;

/**
 * A simple type definition.  Also used for complex type definitions with simpleContent.
 *
 * @author Ryan Heaton
 */
public class SimpleTypeDefinition extends TypeDefinition {

  private final JAXBValidator validator;
  private TypeMirror baseType;

  public SimpleTypeDefinition(ClassDeclaration delegate, JAXBValidator validator) {
    super(delegate);

    this.validator = validator;
    validator.validate(this);
  }

  /**
   * The base type for this simple type.
   *
   * @return The base type for this simple type.
   */
  public TypeMirror getBaseType() {
    return getValue().getAccessorType();
  }

}
