package net.sf.enunciate.examples.xfire_client.schema.animals;

import net.sf.enunciate.examples.xfire_client.schema.*;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Cat extends Figure {

  private Circle face;
  private Triangle[] eyes;
  private Collection<Triangle> ears;
  private Line nose;
  private Collection<Line> whiskers;
  private Line mouth;

  @XmlAttribute
  @XmlIDREF
  public Line getMouth() {
    return mouth;
  }

  public void setMouth(Line mouth) {
    this.mouth = mouth;
  }

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

  @XmlIDREF
  public Collection<Triangle> getEars() {
    return ears;
  }

  public void setEars(Collection<Triangle> ears) {
    this.ears = ears;
  }

  @XmlIDREF
  public Line getNose() {
    return nose;
  }

  public void setNose(Line nose) {
    this.nose = nose;
  }

  public Collection<Line> getWhiskers() {
    return whiskers;
  }

  public void setWhiskers(Collection<Line> whiskers) {
    this.whiskers = whiskers;
  }
}
