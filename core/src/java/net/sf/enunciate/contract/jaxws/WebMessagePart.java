package net.sf.enunciate.contract.jaxws;

import javax.xml.namespace.QName;

/**
 * A part of a complex web message.
 *
 * @author Ryan Heaton
 */
public interface WebMessagePart {

  /**
   * The part name.
   *
   * @return The part name.
   */
  String getPartName();

  /**
   * The documentation for this web message part.
   *
   * @return The documentation for this web message part.
   */
  String getPartDocs();

  /**
   * The qname of the element for this part.
   *
   * @return The qname of the element for this part.
   */
  QName getElementQName();

  /**
   * Whether this web message part defines an implicit schema element.
   *
   * @return Whether this web message part defines an implicit schema element.
   */
  boolean isImplicitSchemaElement();

}
