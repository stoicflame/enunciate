package org.codehaus.enunciate.samples.petclinic.schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@XmlRootElement
public class Owner extends Person {

  private Set<Integer> petIds;

  @XmlElementWrapper (
    name = "petIds"
  )
  @XmlElement (
    name = "petId"
  )
  public Set<Integer> getPetIds() {
    return petIds;
  }

  public void setPetIds(Set<Integer> petIds) {
    this.petIds = petIds;
  }

}
