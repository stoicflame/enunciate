package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.cite;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class Citation {

  private String template;
  private String value;

  @XmlAttribute
  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
