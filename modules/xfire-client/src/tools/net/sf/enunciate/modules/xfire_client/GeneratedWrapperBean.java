package net.sf.enunciate.modules.xfire_client;

import javax.xml.namespace.QName;

/**
 * A marker interface for generated wrapper beans (i.e. request wrappers, response wrappers, implicit fault beans).
 *
 * @author Ryan Heaton
 */
public interface GeneratedWrapperBean {

  /**
   * The qname of the wrapper thread.
   *
   * @return The qname of the wrapper thread.
   */
  public QName getWrapperQName();

}
