package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class XMLIDBean {
  private String id;

  @XmlID
  @XmlValue
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
