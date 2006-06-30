package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

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
        baseType = ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(getSuperclass());
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
  public String getCompositorName() {
    return getPropertyOrder() == null ? "all" : "sequence";
  }

  /**
   * The content type of this complex type definition.
   *
   * @return The content type of this complex type definition.
   */
  public ContentType getContentType() {
    if (!getElements().isEmpty()) {
      if (getBaseType() == KnownXmlType.ANY_TYPE) {
        return ContentType.IMPLIED;
      }
      else {
        return ContentType.COMPLEX;
      }
    }
    else if (getValue() != null) {
      return ContentType.SIMPLE;
    }
    else {
      return ContentType.EMPTY;
    }
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isComplex() {
    return true;
  }

  @Override
  public ValidationResult accept(Validator validator) {
    return validator.validateComplexType(this);
  }

}
