package net.sf.enunciate.examples.xfire_client.schema.structures;

import net.sf.enunciate.examples.xfire_client.schema.Rectangle;
import net.sf.enunciate.examples.xfire_client.schema.Triangle;
import net.sf.enunciate.examples.xfire_client.schema.Circle;
import net.sf.enunciate.examples.xfire_client.schema.Figure;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class House extends Figure {

  private Rectangle base;
  private Triangle roof;
  private Rectangle door;
  private Circle doorKnob;
  private List<Rectangle> windows;

  public Rectangle getBase() {
    return base;
  }

  public void setBase(Rectangle base) {
    this.base = base;
  }

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

  public List<Rectangle> getWindows() {
    return windows;
  }

  public void setWindows(List<Rectangle> windows) {
    this.windows = windows;
  }
}
