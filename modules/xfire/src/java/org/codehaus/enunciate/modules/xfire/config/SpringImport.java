package org.codehaus.enunciate.modules.xfire.config;

import java.io.File;

/**
 * Configuration element used to add spring configuration to the module.
 * 
 * @author Ryan Heaton
 */
public class SpringImport {

  private File file;
  private String Uri;

  /**
   * The spring file to import.
   *
   * @return The spring file to import.
   */
  public File getFile() {
    return file;
  }

  /**
   * The spring file to import.
   *
   * @param file The spring file to import.
   */
  public void setFile(File file) {
    this.file = file;
  }

  /**
   * Used to indicate a URI pointing to the the spring import.
   *
   * @return The URI to the spring import.
   */
  public String getUri() {
    return Uri;
  }

  /**
   * The URI to the spring import.
   *
   * @param uri The URI to the spring import.
   */
  public void setUri(String uri) {
    this.Uri = uri;
  }

}
