package net.sf.enunciate.contract.jaxb;

/**
 * Enum for content type of a complex type def.
 *
 * @author Ryan Heaton
 */
public enum ContentType {

  /**
   * Empty content type.
   */
  EMPTY,

  /**
   * Mixed content type.
   */
  MIXED,

  /**
   * Simple content type.
   */
  SIMPLE,

  /**
   * Complex (element-only) content type.
   */
  COMPLEX

}
