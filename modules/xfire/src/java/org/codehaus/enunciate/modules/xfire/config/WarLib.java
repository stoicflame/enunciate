package org.codehaus.enunciate.modules.xfire.config;

/**
 * Configuration object for specifying a war library.
 *
 * @author Ryan Heaton
 */
public class WarLib {

  private String path;

  /**
   * The path to the library to be added to the war.
   *
   * @return The path to the library to be added to the war.
   */
  public String getPath() {
    return path;
  }

  /**
   * The path to the library to be added to the war.
   *
   * @param path The path to the library to be added to the war.
   */
  public void setPath(String path) {
    this.path = path;
  }
}
