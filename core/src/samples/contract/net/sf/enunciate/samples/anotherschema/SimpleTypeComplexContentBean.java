package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class SimpleTypeComplexContentBean {

  private int property1;
  private String property2;
  private boolean property3;

  @XmlValue
  public int getProperty1() {
    return property1;
  }

  public void setProperty1(int property1) {
    this.property1 = property1;
  }

  @XmlAttribute (
    namespace = "urn:SimpleTypeComplexContentBean.Property2"
  )
  public String getProperty2() {
    return property2;
  }

  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  @XmlAttribute (
    namespace = "urn:SimpleTypeComplexContentBean.Property3"
  )
  public boolean isProperty3() {
    return property3;
  }

  public void setProperty3(boolean property3) {
    this.property3 = property3;
  }
}
