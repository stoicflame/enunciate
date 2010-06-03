package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAnyElement;

/**
 * @author Ryan Heaton
 */
public class RootElementMapAdaptedValue {

  private Object value;

  @XmlAnyElement
  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}