package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class CompoundId {

  private String property1;
  private String property2;
  private String value;

  public CompoundId(String value) {
    this.value = value;
  }

  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  public String getProperty2() {
    return property2;
  }

  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  public String toValue() {
    return value;
  }

}
