package com.webcohesion.enunciate.examples.objc_client.schema;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Dot {

  private String why;

  public String getWhy() {
    return why;
  }

  public void setWhy(String why) {
    this.why = why;
  }
}
