package org.codehaus.enunciate.samples.xfire_client;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * @author Ryan Heaton
 */
public class InvalidComplexType {

  private String[] idrefs;

  @XmlList
  @XmlIDREF
  public String[] getIdrefs() {
    return idrefs;
  }

  public void setIdrefs(String[] idrefs) {
    this.idrefs = idrefs;
  }
}
