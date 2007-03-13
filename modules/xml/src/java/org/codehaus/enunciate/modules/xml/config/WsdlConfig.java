package org.codehaus.enunciate.modules.xml.config;

/**
 * The object used to configure the generation of a schema, overrides the defaults.
 *
 * @author Ryan Heaton
 */
public class WsdlConfig {

  private String namespace;
  private String file;

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * The target namespace.
   *
   * @param namespace The target namespace.
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * The file to which to write this schema.
   *
   * @return The file to which to write this schema.
   */
  public String getFile() {
    return file;
  }

  /**
   * The file to which to write this schema.
   *
   * @param file The file to which to write this schema.
   */
  public void setFile(String file) {
    this.file = file;
  }

}
