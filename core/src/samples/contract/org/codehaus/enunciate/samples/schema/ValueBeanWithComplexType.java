package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class ValueBeanWithComplexType {

  private BeanOne bean;

  @XmlValue
  public BeanOne getBean() {
    return bean;
  }

  public void setBean(BeanOne bean) {
    this.bean = bean;
  }
}
