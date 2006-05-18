package net.sf.enunciate.contract.jaxws;

import java.util.Collection;

/**
 * A complex web message.  Complex messages potentially have multiple parts.
 *
 * @author Ryan Heaton
 */
public interface ComplexWebMessage extends WebMessage {

  /**
   * The parts of this complex input/output.
   *
   * @return The parts of this complex input/output.
   */
  Collection<WebMessagePart> getParts();

}
