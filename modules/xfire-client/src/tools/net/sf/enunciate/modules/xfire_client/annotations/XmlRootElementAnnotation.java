package net.sf.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for an xml root element.
 *
 * @author Ryan Heaton
 */
public class XmlRootElementAnnotation implements Serializable {

  private String name;
  private String namespace;

  public XmlRootElementAnnotation(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  /**
   * The name of the element.
   *
   * @return The name of the element.
   */
  public String name() {
    return this.name;
  }

  /**
   * The namespace of the element.
   *
   * @return The namespace of the element.
   */
  public String namespace() {
    return this.namespace;
  }
}
