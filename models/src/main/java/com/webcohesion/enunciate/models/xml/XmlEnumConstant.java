package com.webcohesion.enunciate.models.xml;

/**
 * @author Ryan Heaton
 */
public class XmlEnumConstant<T> {

  private T value;

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }
}
