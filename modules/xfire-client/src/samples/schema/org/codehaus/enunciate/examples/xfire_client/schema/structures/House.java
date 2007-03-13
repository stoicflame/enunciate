package org.codehaus.enunciate.examples.xfire_client.schema.structures;

import org.codehaus.enunciate.examples.xfire_client.schema.Rectangle;
import org.codehaus.enunciate.examples.xfire_client.schema.Triangle;
import org.codehaus.enunciate.examples.xfire_client.schema.Circle;
import org.codehaus.enunciate.examples.xfire_client.schema.Figure;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class House extends Figure {

  private Rectangle base;
  private Triangle roof;
  private Rectangle door;
  private Circle doorKnob;
  private List<Rectangle> windows;

  @XmlElement (
    required = true
  )
  public Rectangle getBase() {
    return base;
  }

  public void setBase(Rectangle base) {
    this.base = base;
  }

  @XmlElement (
    nillable = true,
    required = true
  )
  public Triangle getRoof() {
    return roof;
  }

  public void setRoof(Triangle roof) {
    this.roof = roof;
  }

  public Rectangle getDoor() {
    return door;
  }

  public void setDoor(Rectangle door) {
    this.door = door;
  }

  public Circle getDoorKnob() {
    return doorKnob;
  }

  public void setDoorKnob(Circle doorKnob) {
    this.doorKnob = doorKnob;
  }

  @XmlElementWrapper (
    nillable = true
  )
  public List<Rectangle> getWindows() {
    return windows;
  }

  public void setWindows(List<Rectangle> windows) {
    this.windows = windows;
  }
}
