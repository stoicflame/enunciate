package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
public class BeanWithPropertyAdaptedBySubclass {

  private BeanTwo beanTwo;

  @XmlJavaTypeAdapter(IdOnlyXmlAdapter.class)
  public BeanTwo getBeanTwo() {
    return beanTwo;
  }

  public void setBeanTwo(BeanTwo beanTwo) {
    this.beanTwo = beanTwo;
  }
}
