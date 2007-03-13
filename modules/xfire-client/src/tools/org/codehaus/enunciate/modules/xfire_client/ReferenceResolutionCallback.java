package org.codehaus.enunciate.modules.xfire_client;

/**
 * Interface used to encapsulate callback logic for resolving a reference.
 *
 * @author Ryan Heaton
 */
public interface ReferenceResolutionCallback {

  /**
   * Handle the resolution of a reference.
   *
   * @param resolution The resolution.
   */
  void handleResolution(Object resolution);

}
