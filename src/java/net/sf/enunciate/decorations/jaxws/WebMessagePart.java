package net.sf.enunciate.decorations.jaxws;

/**
 * A part of a complex web message.
 *
 * @author Ryan Heaton
 */
public interface WebMessagePart extends WebMessage {

  /**
   * The part name.
   *
   * @return The part name.
   */
  String getPartName();

  /**
   * The local element name.
   *
   * @return The local element name.
   */
  String getName();

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  String getTargetNamespace();

}
