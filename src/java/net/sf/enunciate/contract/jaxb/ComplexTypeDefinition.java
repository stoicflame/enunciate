package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.contract.jaxb.types.XmlTypeDecorator;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.util.QName;

/**
 * A complex type definition.
 *
 * @author Ryan Heaton
 */
public class ComplexTypeDefinition extends SimpleTypeDefinition {

  public ComplexTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  @Override
  public XmlTypeMirror getBaseType() {
    XmlTypeMirror baseType = super.getBaseType();

    if (baseType == null) {
      try {
        baseType = XmlTypeDecorator.decorate(getSuperclass());
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), e.getMessage());
      }
    }

    return baseType;
  }

  /**
   * The compositor for this type definition.
   *
   * @return The compositor for this type definition.
   */
  public QName getCompositor() {
    String value = getPropertyOrder() == null ? "all" : "sequence";
    return new QName("http://www.w3.org/2001/XMLSchema", value);
  }

  /**
   * The content type of this complex type definition.
   *
   * @return The content type of this complex type definition.
   */
  public ContentType getContentType() {
    if (!getElements().isEmpty()) {
      return ContentType.COMPLEX;
    }
    else if (getValue() != null) {
      return ContentType.SIMPLE;
    }
    else {
      return ContentType.EMPTY;
    }
  }

  @Override
  public ValidationResult accept(Validator validator) {
    return validator.validateComplexType(this);
  }

}
