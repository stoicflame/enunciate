package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

/**
 * @author Ryan Heaton
 */
public class InvalidElementRefBean
{
  private ElementBeanOne property1;

  @XmlElementRef
  @XmlElement
  public ElementBeanOne getProperty1() {
    return property1;
  }

  public void setProperty1(ElementBeanOne property1) {
    this.property1 = property1;
  }

}
