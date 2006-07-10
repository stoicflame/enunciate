package net.sf.enunciate.contract.jaxws;

import java.util.Collection;

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

  /**
   * The parts of this complex input/output.
   *
   * @return The parts of this complex input/output.
   */
  Collection<WebMessagePart> getParts();
}
