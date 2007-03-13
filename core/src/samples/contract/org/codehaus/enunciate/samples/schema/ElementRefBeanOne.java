package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.JAXBElement;

/**
 * @author Ryan Heaton
 */
public class ElementRefBeanOne {

  private ElementBeanOne property1;
  private Object property2;
  private JAXBElement<BeanOne> property3;

  @XmlElementRef
  public ElementBeanOne getProperty1() {
    return property1;
  }

  public void setProperty1(ElementBeanOne property1) {
    this.property1 = property1;
  }

  @XmlElementRef (
    type=BeanThree.class
  )
  public Object getProperty2() {
    return property2;
  }

  public void setProperty2(Object property2) {
    this.property2 = property2;
  }

  @XmlElementRef (
    name = "beanone",
    namespace = "urn:beanone"
  )
  public JAXBElement<BeanOne> getProperty3() {
    return property3;
  }

  public void setProperty3(JAXBElement<BeanOne> property3) {
    this.property3 = property3;
  }

}
