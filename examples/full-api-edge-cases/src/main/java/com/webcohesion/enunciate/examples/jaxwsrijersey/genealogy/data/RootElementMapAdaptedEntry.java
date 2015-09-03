package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class RootElementMapAdaptedEntry {

  private String key;
  private RootElementMapAdaptedValue value;

  public RootElementMapAdaptedValue getValue() {
    return value;
  }

  public void setValue(RootElementMapAdaptedValue value) {
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}