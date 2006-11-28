package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class InvalidElementBean {

  private Collection<Integer> ints;

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

}
