package org.codehaus.enunciate.contract.jaxb.types;

import javax.xml.namespace.QName;

/**
 * Type mirror that provides its qname.
 *
 * @author Ryan Heaton
 */
public interface XmlTypeMirror {

  /**
   * The (local) name of this xml type.
   *
   * @return The (local) name of this xml type.
   */
  String getName();

  /**
   * The namespace for this xml type.
   *
   * @return The namespace for this xml type.
   */
  String getNamespace();

  /**
   * The qname of the xml type mirror.
   *
   * @return The qname of the xml type mirror.
   */
  QName getQname();

  /**
   * Whether this type is anonymous.
   *
   * @return Whether this type is anonymous.
   */
  boolean isAnonymous();

  /**
   * Whether this is a simple XML type.
   *
   * @return Whether this is a simple XML type.
   */
  boolean isSimple();

}
