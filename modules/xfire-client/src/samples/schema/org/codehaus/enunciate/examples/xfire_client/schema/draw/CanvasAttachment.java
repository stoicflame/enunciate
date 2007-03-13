package org.codehaus.enunciate.examples.xfire_client.schema.draw;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class CanvasAttachment {

  private byte[] value;

  @XmlValue
  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }
}
