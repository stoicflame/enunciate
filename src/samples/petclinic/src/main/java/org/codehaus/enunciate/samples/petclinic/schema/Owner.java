package org.codehaus.enunciate.samples.petclinic.schema;

import java.util.Set;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Owner extends Person {

  private Set<Integer> petIds;

  public Set<Integer> getPetIds() {
    return petIds;
  }

  public void setPetIds(Set<Integer> petIds) {
    this.petIds = petIds;
  }

}
