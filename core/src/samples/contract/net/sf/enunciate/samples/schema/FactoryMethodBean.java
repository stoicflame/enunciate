package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType (
  factoryClass = FactoryBeanFactory.class,
  factoryMethod = "createBean"
)
public class FactoryMethodBean {
}
