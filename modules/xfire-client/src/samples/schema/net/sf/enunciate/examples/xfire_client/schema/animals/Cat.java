package net.sf.enunciate.examples.xfire_client.schema.animals;

import net.sf.enunciate.examples.xfire_client.schema.*;

import javax.xml.bind.annotation.XmlElementRef;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class Cat extends Figure {

  private Circle face;
  private Triangle[] eyes;
  private Collection<Triangle> ears;
  private Rectangle nose;
  private Collection<Line> whiskers;

  @XmlElementRef
  public Circle getFace() {
    return face;
  }

  public void setFace(Circle face) {
    this.face = face;
  }

  public Triangle[] getEyes() {
    return eyes;
  }

  public void setEyes(Triangle[] eyes) {
    this.eyes = eyes;
  }

  public Collection<Triangle> getEars() {
    return ears;
  }

  public void setEars(Collection<Triangle> ears) {
    this.ears = ears;
  }

  public Rectangle getNose() {
    return nose;
  }

  public void setNose(Rectangle nose) {
    this.nose = nose;
  }

  public Collection<Line> getWhiskers() {
    return whiskers;
  }

  public void setWhiskers(Collection<Line> whiskers) {
    this.whiskers = whiskers;
  }
}
