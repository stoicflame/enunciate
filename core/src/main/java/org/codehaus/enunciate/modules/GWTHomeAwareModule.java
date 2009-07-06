package org.codehaus.enunciate.modules;

/**
 * @author Ryan Heaton
 */
public interface GWTHomeAwareModule {

  /**
   * Set the GWT home directory.
   *
   * @param gwtHome The GWT home directory.
   */
  void setGwtHome(String gwtHome);
}
