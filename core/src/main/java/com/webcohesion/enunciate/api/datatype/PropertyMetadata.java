package com.webcohesion.enunciate.api.datatype;

/**
 * @author Ryan Heaton
 */
public class PropertyMetadata {

  private final String value;
  private final String title;
  private final String href;

  public PropertyMetadata(String value, String title, String href) {
    this.value = value;
    this.title = title;
    this.href = href;
  }

  public final boolean isStructure() {
    return true;
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
