package net.sf.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class EMail {

  private String value;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
