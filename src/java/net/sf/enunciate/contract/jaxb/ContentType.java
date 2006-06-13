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
  COMPLEX;

  /**
   * Whether this is the empty content type.
   *
   * @return Whether this is the empty content type.
   */
  public boolean isEmpty() {
    return this == EMPTY;
  }

  /**
   * Whether this is the mixed content type.
   *
   * @return Whether this is the mixed content type.
   */
  public boolean isMixed() {
    return this == MIXED;
  }

  /**
   * Whether this is the simple content type.
   *
   * @return Whether this is the simple content type.
   */
  public boolean isSimple() {
    return this == SIMPLE;
  }

  /**
   * Whether this is the complex content type.
   *
   * @return Whether this is the complex content type.
   */
  public boolean isComplex() {
    return this == COMPLEX;
  }
}
