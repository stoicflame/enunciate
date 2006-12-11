package net.sf.enunciate.examples.xfire_client.schema;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Triangle extends Shape {

  private int base;
  private int height;

  public int getBase() {
    return base;
  }

  public void setBase(int base) {
    this.base = base;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
