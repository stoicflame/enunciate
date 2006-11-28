package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
public class Gender extends Assertion {

  private GenderType type;

  @XmlAttribute
  public GenderType getType() {
    return type;
  }

  public void setType(GenderType type) {
    this.type = type;
  }
}
