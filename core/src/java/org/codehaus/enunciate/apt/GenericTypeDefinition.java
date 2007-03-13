package org.codehaus.enunciate.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;

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
