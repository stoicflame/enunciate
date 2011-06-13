package org.codehaus.enunciate.samples.petclinic.schema;

import java.util.Collection;
import java.util.Set;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Vet<H extends Hobby> extends Person {

  private Set<Specialty> specialties;
  private Collection<VetType> types;
  private H hobby;

  public Set<Specialty> getSpecialties() {
    return specialties;
  }

  public void setSpecialties(Set<Specialty> specialties) {
    this.specialties = specialties;
  }

  public Collection<VetType> getTypes() {
    return types;
  }

  public void setTypes(Collection<VetType> types) {
    this.types = types;
  }

  public H getHobby() {
    return hobby;
  }

  public void setHobby(H hobby) {
    this.hobby = hobby;
  }
}
