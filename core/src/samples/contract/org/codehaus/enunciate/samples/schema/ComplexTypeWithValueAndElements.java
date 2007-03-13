package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class ComplexTypeWithValueAndElements {

  private String value;
  private int element1;
  private boolean element2;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getElement1() {
    return element1;
  }

  public void setElement1(int element1) {
    this.element1 = element1;
  }

  public boolean isElement2() {
    return element2;
  }

  public void setElement2(boolean element2) {
    this.element2 = element2;
  }

  public class NestedNotAType {

    private String nestedProperty;

    public String getNestedProperty() {
      return nestedProperty;
    }

    public void setNestedProperty(String nestedProperty) {
      this.nestedProperty = nestedProperty;
    }
  }
}
