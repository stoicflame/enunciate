package net.sf.enunciate.contract.jaxws;

import javax.xml.namespace.QName;

/**
 * An implicit child element.
 *
 * @author Ryan Heaton
 */
public interface ImplicitChildElement extends ImplicitSchemaElement {

  /**
   * The value for the min occurs of the child element.
   *
   * @return The value for the min occurs of the child element.
   */
  public int getMinOccurs();

  /**
   * The value for the max occurs of the child element.
   *
   * @return The value for the max occurs of the child element.
   */
  public String getMaxOccurs();

  /**
   * The qname of the type for this element.  Since child element types cannot be anonymous, this value must not be null.
   *
   * @return The qname of the type for this element.
   */
  QName getTypeQName();

}
