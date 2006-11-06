package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

/**
 * An accessor that is marshalled in xml to an xml attribute.
 *
 * @author Ryan Heaton
 */
public class Attribute extends Accessor {

  private final XmlAttribute xmlAttribute;

  public Attribute(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);

    xmlAttribute = getAnnotation(XmlAttribute.class);
  }

  // Inherited.
  public String getName() {
    String name = getSimpleName();

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.name()))) {
      name = xmlAttribute.name();
    }

    return name;
  }

  // Inherited.
  public String getNamespace() {
    String namespace = getTypeDefinition().getNamespace();

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.namespace()))) {
      namespace = xmlAttribute.namespace();
    }

    return namespace;
  }


  /**
   * An attribute is a ref if its namespace differs from that of its type definition (JAXB spec 8.9.7.2).
   *
   * @return The ref or null.
   */
  @Override
  public QName getRef() {
    String namespace = getNamespace();
    namespace = namespace == null ? "" : namespace;
    String typeNamespace = getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;

    if (!namespace.equals(typeNamespace)) {
      return new QName(namespace, getName());
    }

    return null;
  }

  /**
   * Whether the attribute is required.
   *
   * @return Whether the attribute is required.
   */
  public boolean isRequired() {
    boolean required = false;

    if (xmlAttribute != null) {
      required = xmlAttribute.required();
    }

    return required;
  }
}
