package net.sf.enunciate.examples.xfire_client.schema;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
public abstract class Shape {

  private String id;
  private Color color;
  private LineStyle lineStyle;
  private int positionX;
  private int positionY;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getPositionX() {
    return positionX;
  }

  public void setPositionX(int positionX) {
    this.positionX = positionX;
  }

  public int getPositionY() {
    return positionY;
  }

  public void setPositionY(int positionY) {
    this.positionY = positionY;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public LineStyle getLineStyle() {
    return lineStyle;
  }

  public void setLineStyle(LineStyle lineStyle) {
    this.lineStyle = lineStyle;
  }
}
