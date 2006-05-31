package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.contract.jaxb.validation.JAXBValidator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema root element declaration.
 *
 * @author Ryan Heaton
 */
public class RootElementDeclaration extends DecoratedClassDeclaration {

  private final XmlRootElement rootElement;
  private final TypeDefinition typeDefinition;
  private final Schema schema;
  private final JAXBValidator validator;

  public RootElementDeclaration(ClassDeclaration delegate, TypeDefinition typeDefinition, JAXBValidator validator) {
    super(delegate);

    this.rootElement = getAnnotation(XmlRootElement.class);
    this.typeDefinition = typeDefinition;
    this.schema = new Schema(delegate.getPackage());
    this.validator = validator;
    validator.validate(this);
  }

  /**
   * The type definition for this root element.
   *
   * @return The type definition for this root element.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  /**
   * The name of the xml element declaration.
   *
   * @return The name of the xml element declaration.
   */
  public String getName() {
    String name = getSimpleName();

    if ((rootElement != null) && (!"##default".equals(rootElement.name()))) {
      name = rootElement.name();

      if ("".equals(name)) {
        name = null;
      }
    }

    return name;
  }

  /**
   * The namespace of the xml element.
   *
   * @return The namespace of the xml element.
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
