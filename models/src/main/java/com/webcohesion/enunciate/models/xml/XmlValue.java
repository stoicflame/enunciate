package com.webcohesion.enunciate.models.xml;

/**
 * @author Ryan Heaton
 */
public class XmlValue {

  private String namespace;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public boolean isAttribute() {
    return false;
  }

  public boolean isElement() {
    return true;
  }

  public boolean isValue() {
    return false;
  }


}
