package org.codehaus.enunciate.modules.amf;

/**
 * Marker interface for objects aware of their AMF mapper.
 *
 * @author Ryan Heaton
 */
public interface AMFMapperAware {

  /**
   * Load the AMF mapper.
   *
   * @return the AMF mapper.
   */
  AMFMapper loadAMFMapper();
  
}
