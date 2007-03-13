package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType (
  factoryClass = FactoryBeanFactory.class,
  factoryMethod = "invalidCreateBean"
)
public class InvalidFactoryMethodBean {

  @XmlType (
    factoryClass = FactoryBeanFactory.class,
    factoryMethod = "dummyMethod"
  )
  public static class UnknownMethodBean {

  }
}
