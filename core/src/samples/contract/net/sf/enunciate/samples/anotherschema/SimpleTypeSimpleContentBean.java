package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class SimpleTypeSimpleContentBean {

  private int property1;

  @XmlValue
  public int getProperty1() {
    return property1;
  }

  public void setProperty1(int property1) {
    this.property1 = property1;
  }

}
