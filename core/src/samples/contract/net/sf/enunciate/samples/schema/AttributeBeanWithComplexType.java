package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
public class AttributeBeanWithComplexType {

  private BeanOne bean;

  @XmlAttribute
  public BeanOne getBean() {
    return bean;
  }

  public void setBean(BeanOne bean) {
    this.bean = bean;
  }
}
