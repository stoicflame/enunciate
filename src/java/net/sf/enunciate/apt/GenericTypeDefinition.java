package net.sf.enunciate.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;

/**
 * Internal class used to inherit some functionality for determining whether a declaration is a simple type
 * or a complex type.
 */
class GenericTypeDefinition extends TypeDefinition {

  protected GenericTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  public ValidationResult accept(Validator validator) {
    return new ValidationResult();
  }

  public XmlTypeMirror getBaseType() {
    return KnownXmlType.ANY_TYPE;
  }
}
