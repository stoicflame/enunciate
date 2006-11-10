package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class InvalidElementBean {

  private Collection<Integer> ints;
  private short prop2;
  private Collection<Double> doubles;

  @XmlElements (
    {@XmlElement (type = Integer.class),
    @XmlElement (type = Short.class)}
  )
  public Collection<Integer> getInts() {
    return ints;
  }

  public void setInts(Collection<Integer> ints) {
    this.ints = ints;
  }

  @XmlElement (
    namespace = "urn:different"
  )
  public short getProp2() {
    return prop2;
  }

  public void setProp2(short prop2) {
    this.prop2 = prop2;
  }

  @XmlElementWrapper (
    namespace = "urn:different"
  )
  public Collection<Double> getDoubles() {
    return doubles;
  }

  public void setDoubles(Collection<Double> doubles) {
    this.doubles = doubles;
  }
}
