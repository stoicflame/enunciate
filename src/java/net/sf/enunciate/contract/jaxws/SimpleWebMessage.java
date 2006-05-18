package net.sf.enunciate.contract.jaxws;

/**
 * A simple web message, i.e. one that has only a single part (e.g. headers, faults).
 *
 * @author Ryan Heaton
 */
public interface SimpleWebMessage extends WebMessage {

  /**
   * The part name for this web message.
   *
   * @return The part name for this web message.
   */
  String getPartName();

  /**
   * The name of this web message.
   *
   * @return The name of this web message.
   */
  String getName();

  /**
   * The target namespace of this web message.
   *
   * @return The target namespace of this web message.
   */
  String getTargetNamespace();

}
