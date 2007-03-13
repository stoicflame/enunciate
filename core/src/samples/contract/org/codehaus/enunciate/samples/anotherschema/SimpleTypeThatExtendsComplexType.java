package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class SimpleTypeThatExtendsComplexType {

  private SimpleTypeComplexContentBean value;

  @XmlValue
  public SimpleTypeComplexContentBean getValue() {
    return value;
  }

  public void setValue(SimpleTypeComplexContentBean value) {
    this.value = value;
  }
}
