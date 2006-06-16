package net.sf.enunciate.contract.jaxb.types;

/**
 * Exception stating that an type is unknown or invalid.
 *
 * @author Ryan Heaton
 */
public class XmlTypeException extends Exception {

  public XmlTypeException(String message) {
    super(message);
  }

}
