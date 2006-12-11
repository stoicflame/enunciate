package net.sf.enunciate.examples.xfire_client.schema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class Label {

  private String value;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
