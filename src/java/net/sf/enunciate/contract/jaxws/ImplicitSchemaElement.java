package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.util.QName;

/**
 * An implicit schema element.  Implied by "literal" SOAP use.
 *
 * @author Ryan Heaton
 */
public interface ImplicitSchemaElement {

  /**
   * The local element name.
   *
   * @return The local element name.
   */
  String getElementName();

  /**
   * The qname of the type for this element, if the type is not anonymous.
   *
   * @return The qname of the type for this element, or null if it's an anonymous type.
   */
  QName getTypeQName();

}
