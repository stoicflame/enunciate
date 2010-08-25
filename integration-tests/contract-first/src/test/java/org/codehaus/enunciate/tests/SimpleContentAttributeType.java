package org.codehaus.enunciate.tests;

import epcglobal.epcis_masterdata.xsd._1.AttributeType;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class SimpleContentAttributeType extends AttributeType {

  private String value;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
