package net.sf.enunciate.contract.jaxws;

/**
 * A web message.  This could in include rpc-style parameters, web faults, header parameters, or in the case
 * of a document/literal wrapped method, the complex aggregate of the non-header input or output parameters.
 * <p/>
 * Each web method consists of a set of web messages.
 *
 * @author Ryan Heaton
 */
public interface WebMessage {

  /**
   * The name of this web message.
   *
   * @return The name of this web message.
   */
  String getMessageName();

  /**
   * Whether this message is simple (e.g. header, fault).
   *
   * @return Whether this message is simple (e.g. header, fault).
   */
  boolean isSimple();

  /**
   * Whether this method is complex, meaning it potentially has multiple parts.
   *
   * @return Whether this method is complex, meaning it potentially has multiple parts.
   */
  boolean isComplex();

  /**
   * Whether this is an input message.
   *
   * @return Whether this is an input message.
   */
  boolean isInput();

  /**
   * Whether this is an output message.
   *
   * @return Whether this is an output message.
   */
  boolean isOutput();

  /**
   * Whether this message is a header parameter.
   *
   * @return Whether this message is a header parameter.
   */
  boolean isHeader();

  /**
   * Whether this message is a web fault.
   *
   * @return Whether this message is a web fault.
   */
  boolean isFault();
}
