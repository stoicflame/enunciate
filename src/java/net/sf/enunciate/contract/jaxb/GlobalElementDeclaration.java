package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema element declaration with global scope.
 *
 * @author Ryan Heaton
 */
public class GlobalElementDeclaration extends DecoratedClassDeclaration {

  private final XmlRootElement rootElement;
  private final TypeDefinition typeDefinition;
  private final Schema schema;
  private final JAXBValidator validator;

  public GlobalElementDeclaration(ClassDeclaration delegate, TypeDefinition typeDefinition, JAXBValidator validator) {
    super(delegate);

    this.rootElement = getAnnotation(XmlRootElement.class);
    this.typeDefinition = typeDefinition;
    this.schema = new Schema(delegate.getPackage());
    this.validator = validator;
    validator.validate(this);
  }

  public TypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  /**
   * The namespace of the xml type element.
   *
   * @return The namespace of the xml type element.
   */
  public String getTargetNamespace() {
    String namespace = getPackage().getNamespace();

    if ((rootElement != null) && (!"##default".equals(rootElement.namespace()))) {
      namespace = rootElement.namespace();
    }

    return namespace;
  }

  /**
   * The schema for this complex type.
   *
   * @return The schema for this complex type.
   */
  public Schema getSchema() {
    return schema;
  }

  // Inherited.
  @Override
  public Schema getPackage() {
    return getSchema();
  }
}
