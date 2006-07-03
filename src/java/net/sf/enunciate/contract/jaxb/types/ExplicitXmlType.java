package net.sf.enunciate.contract.jaxb.types;

import net.sf.enunciate.util.QName;

/**
 * An explicit xml type.
 *
 * @author Ryan Heaton
 */
public class ExplicitXmlType implements XmlTypeMirror {

  private final String name;
  private final String namespace;

  public ExplicitXmlType(String name, String namespace) {
    this.name = name;
    this.namespace = namespace;
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  public boolean isAnonymous() {
    return false;
  }
}
