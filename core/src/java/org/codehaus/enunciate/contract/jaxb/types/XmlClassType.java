package org.codehaus.enunciate.contract.jaxb.types;

import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

import javax.xml.namespace.QName;

/**
 * Decorator for an xml class type.
 *
 * @author Ryan Heaton
 */
public class XmlClassType implements XmlTypeMirror {

  private final TypeDefinition typeDef;

  public XmlClassType(TypeDefinition typeDef) {
    if (typeDef == null) {
      throw new IllegalArgumentException("A type definition must be supplied.");
    }

    this.typeDef = typeDef;
  }

  /**
   * The name of a class type depends on its type definition.
   *
   * @return The name of a class type depends on its type definition.
   */
  public String getName() {
    return this.typeDef.getName();
  }

  /**
   * The namespace of a class type depends on its type definition.
   *
   * @return The namespace of a class type depends on its type definition.
   */
  public String getNamespace() {
    return this.typeDef.getNamespace();
  }

  /**
   * The qname.
   *
   * @return The qname.
   */
  public QName getQname() {
    String localPart = getName();
    if (localPart == null) {
      localPart = "";
    }
    return new QName(getNamespace(), localPart);
  }

  /**
   * Whether a class type is anonymous depends on its type definition.
   *
   * @return Whether this class type is anonymous.
   */
  public boolean isAnonymous() {
    return this.typeDef.isAnonymous();
  }

  /**
   * This type is simple if its type definition is simple.
   *
   * @return This type is simple if its type definition is simple.
   */
  public boolean isSimple() {
    return this.typeDef.isSimple();
  }

  /**
   * Get the type definition for this class type.
   *
   * @return The type definition for this class type.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDef;
  }

}
