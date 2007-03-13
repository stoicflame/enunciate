package org.codehaus.enunciate.contract.jaxws;

import java.util.Collection;

/**
 * An implicit root schema element.
 *
 * @author Ryan Heaton
 */
public interface ImplicitRootElement extends ImplicitSchemaElement {

  /**
   * If the schema type of this element is anonymous, get the list of child elements for this schema element.
   *
   * @return The list of child elements for this schema element, or null if the type of this implicit element is not anonymous.
   */
  Collection<ImplicitChildElement> getChildElements();

}
