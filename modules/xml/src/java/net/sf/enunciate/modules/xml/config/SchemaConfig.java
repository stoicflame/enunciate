package net.sf.enunciate.modules.xml.config;

/**
 * The object used to configure the generation of a schema, overrides the defaults.
 *
 * @author Ryan Heaton
 */
public class SchemaConfig {

  private String namespace;
  private String file;
  private String location;
  private String elementFormDefault;
  private String attributeFormDefault;

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
    if ("".equals(namespace)) {
      namespace = null;
    }

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

  /**
   * The schema location.
   *
   * @return The schema location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * The schema location.
   *
   * @param location The schema location.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * The elementFormDefault for this schema.
   *
   * @return The elementFormDefault for this schema.
   */
  public String getElementFormDefault() {
    return elementFormDefault;
  }

  /**
   * The elementFormDefault for this schema.
   *
   * @param elementFormDefault The elementFormDefault for this schema.
   */
  public void setElementFormDefault(String elementFormDefault) {
    this.elementFormDefault = elementFormDefault;
  }

  /**
   * The attributeFormDefault for this schema.
   *
   * @return The attributeFormDefault for this schema.
   */
  public String getAttributeFormDefault() {
    return attributeFormDefault;
  }

  /**
   * The attributeFormDefault for this schema.
   *
   * @param attributeFormDefault The attributeFormDefault for this schema.
   */
  public void setAttributeFormDefault(String attributeFormDefault) {
    this.attributeFormDefault = attributeFormDefault;
  }

}
