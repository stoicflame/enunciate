package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.*;

/**
 * @author Ryan Heaton
 */
@XmlAccessorType (
  XmlAccessType.PROPERTY
)
public class FullTypeDefBeanOne {

  private String property1;
  private String property2;
  private String property3;
  private ElementBeanOne property4;
  private String property5;

  @XmlAttribute
  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  @XmlValue
  public String getProperty2() {
    return property2;
  }

  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  @XmlElement
  @XmlID
  public String getProperty3() {
    return property3;
  }

  public void setProperty3(String property3) {
    this.property3 = property3;
  }

  @XmlElementRef
  public ElementBeanOne getProperty4() {
    return property4;
  }

  public void setProperty4(ElementBeanOne property4) {
    this.property4 = property4;
  }

  public String getProperty5() {
    return property5;
  }

  public void setProperty5(String property5) {
    this.property5 = property5;
  }

}
