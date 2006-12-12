package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.MessageWriter;

/**
 * Marker interface for xfire types that know how to write just the value of their xml ids.
 *
 * @author Ryan Heaton
 */
public interface XmlIDType {

  /**
   * Write the value of the xml id of the specified object to the specified writer.
   *
   * @param instance The instance.
   * @param writer The writer.
   */
  void writeXmlID(Object instance, MessageWriter writer);

}
