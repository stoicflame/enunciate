package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;
import net.sf.enunciate.util.QName;

/**
 * A complex type definition.
 *
 * @author Ryan Heaton
 */
public class ComplexTypeDefinition extends TypeDefinition {

  private final JAXBValidator validator;

  public ComplexTypeDefinition(ClassDeclaration delegate, JAXBValidator validator) {
    super(delegate);

    this.validator = validator;
    validator.validate(this);
  }

  /**
   * The base type definition, or null if the distinguished ur-type definition.
   *
   * @return The base type definition, or null if the distinguished ur-type definition.
   */
  public TypeDefinition getBaseTypeDefinition() {
    ClassType superclass = getSuperclass();
    if (superclass != null) {
      ClassDeclaration declaration = superclass.getDeclaration();
      if (declaration != null) {
        if (!Object.class.getName().equals(declaration.getQualifiedName())) {
          return new ComplexTypeDefinition(declaration, this.validator);
        }
      }
    }

    return null;
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

  }

}
