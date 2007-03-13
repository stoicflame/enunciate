package org.codehaus.enunciate.modules.xfire.config;

import java.io.File;

/**
 * Configuration element used to specify a pattern of resources to copy.
 *
 * @author Ryan Heaton
 */
public class CopyResources {

  private String pattern;
  private File dir;

  /**
   * The matching pattern for the resources to copy.
   *
   * @return The matching pattern for the resources to copy.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * The matching pattern for the resources to copy.
   *
   * @param pattern The matching pattern for the resources to copy.
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  /**
   * The base directory for the resource copy.
   *
   * @return The base directory for the resource copy.
   */
  public File getDir() {
    return dir;
  }

  /**
   * The base directory for the resource copy.
   *
   * @param dir The base directory for the resource copy.
   */
  public void setDir(File dir) {
    this.dir = dir;
  }
}
