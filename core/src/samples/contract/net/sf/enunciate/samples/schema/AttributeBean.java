package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType (
  namespace = "urn:attributebean"
)
public class AttributeBean {

  private String property1;
  private int property2;
  private boolean property3;

  @XmlAttribute (
    namespace = "urn:other"
  )
  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  @XmlAttribute (
    name="dummyname"
  )
  public int getProperty2() {
    return property2;
  }

  public void setProperty2(int property2) {
    this.property2 = property2;
  }

  @XmlAttribute (
    required = true
  )
  public boolean isProperty3() {
    return property3;
  }

  public void setProperty3(boolean property3) {
    this.property3 = property3;
  }

}
