package org.codehaus.enunciate.samples.petclinic.schema;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Simple JavaBean domain object with an id property.
 * Used as a base class for objects needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Entity {

  private Integer id;

  public void setId(Integer id) {
    this.id = id;
  }

  @XmlAttribute
  public Integer getId() {
    return id;
  }

  public boolean isNew() {
    return (this.id == null);
  }

}
