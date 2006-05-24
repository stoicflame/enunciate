package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;

/**
 * @author Ryan Heaton
 */
public class SimpleTypeDefinition extends TypeDefinition {

  private final JAXBValidator validator;

  public SimpleTypeDefinition(ClassDeclaration delegate, JAXBValidator validator) {
    super(delegate);

    this.validator = validator;
    validator.validate(this);
  }

}
