package net.sf.enunciate.examples.xfire_client.schema.vehicles;

import net.sf.enunciate.examples.xfire_client.schema.Figure;
import net.sf.enunciate.examples.xfire_client.schema.Rectangle;
import net.sf.enunciate.examples.xfire_client.schema.Circle;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Bus extends Figure {

  private Rectangle frame;
  private Circle[] wheels;
  private Collection<Rectangle> windows;
  private Rectangle door;

  public Rectangle getFrame() {
    return frame;
  }

  public void setFrame(Rectangle frame) {
    this.frame = frame;
  }

  public Circle[] getWheels() {
    return wheels;
  }

  public void setWheels(Circle[] wheels) {
    this.wheels = wheels;
  }

  @XmlElementWrapper (
    name = "windows"
  )
  public Collection<Rectangle> getWindows() {
    return windows;
  }

  public void setWindows(Collection<Rectangle> windows) {
    this.windows = windows;
  }

  public Rectangle getDoor() {
    return door;
  }

  public void setDoor(Rectangle door) {
    this.door = door;
  }
}
