package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedClassDeclaration {

  protected final XmlType xmlType;
  private final Schema schema;

  protected TypeDefinition(ClassDeclaration delegate) {
    super(delegate);

    this.xmlType = getAnnotation(XmlType.class);
    this.schema = new Schema(delegate.getPackage());
  }

  /**
   * The namespace of the xml type element.
   *
   * @return The namespace of the xml type element.
   */
  public String getTargetNamespace() {
    String namespace = getPackage().getNamespace();

    if ((xmlType != null) && (!"##default".equals(xmlType.namespace()))) {
      namespace = xmlType.namespace();
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
