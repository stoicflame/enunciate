package org.codehaus.enunciate.contract.jaxws;

import javax.xml.namespace.QName;

/**
 * A part of a complex web message.
 *
 * @author Ryan Heaton
 */
public interface WebMessagePart {

  public enum ParticleType {
    ELEMENT,
    TYPE
  }

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
   * The particle type for this part.
   *
   * @return The particle type for this part.
   */
  ParticleType getParticleType();

  /**
   * The qname of the schema particle (element or type) for this part.
   *
   * @return The qname of the schema particle (element or type) for this part.
   */
  QName getParticleQName();

  /**
   * Whether this web message part defines an implicit schema element.
   *
   * @return Whether this web message part defines an implicit schema element.
   */
  boolean isImplicitSchemaElement();

}
