package org.codehaus.enunciate.samples.petclinic.schema;

import java.util.Collection;
import java.util.Set;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Vet extends Person {

  private Set<Specialty> specialties;
  private Collection<VetType> types;

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
}
