package org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class NestedSimpleType {

  private String value;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
