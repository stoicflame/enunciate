package org.codehaus.enunciate.samples.petclinic.schema;

import java.util.Date;
import java.util.Set;

/**
 * Simple JavaBean business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Pet extends NamedEntity {

  private Date birthDate;
  private PetType type;
  private Integer ownerId;
  private Set<Visit> visits;

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public Date getBirthDate() {
    return this.birthDate;
  }

  public void setType(PetType type) {
    this.type = type;
  }

  public PetType getType() {
    return type;
  }

  public Integer getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Integer ownerId) {
    this.ownerId = ownerId;
  }

  public Set<Visit> getVisits() {
    return visits;
  }

  public void setVisits(Set<Visit> visits) {
    this.visits = visits;
  }
}
