package com.webcohesion.enunciate.api.datatype;

/**
 * @author Ryan Heaton
 */
public class PropertyMetadata {

  private final boolean structure;
  private final String value;
  private final String title;
  private final String href;

  public PropertyMetadata(String value, String title, String href) {
    this.value = value;
    this.title = title;
    this.href = href;
    this.structure = true;
  }

  public PropertyMetadata(String value) {
    this.value = value;
    this.title = null;
    this.href = null;
    this.structure = false;
  }

  public final boolean isStructure() {
    return this.structure;
  }

  public String getValue() {
    return value;
  }

  public String getTitle() {
    return title;
  }

  public String getHref() {
    return href;
  }

  @Override
  public String toString() {
    return getValue();
  }
}
