package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.TypeMirror;
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
  public TypeMirror getBaseType() {
    TypeMirror baseType = super.getBaseType();

    if (baseType == null) {
      baseType = getSuperclass();
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

}
