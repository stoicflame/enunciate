package net.sf.enunciate.modules.xfire_client;

import javax.xml.namespace.QName;

/**
 * Marker interface for xfire types that also are root xml elements.
 *
 * @author Ryan Heaton
 */
public interface RootElementType {

  /**
   * The qname of the xml root element.
   *
   * @return The qname of the xml root element.
   */
  public QName getRootElementName();
  
}
