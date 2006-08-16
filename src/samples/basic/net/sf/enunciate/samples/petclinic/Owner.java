/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * A pet owner.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://net.sf.enunciate/samples/petclinic/owners"
)
public class Owner extends Person {

  private Set<Pet> pets;

  protected void setPetsInternal(Set<Pet> pets) {
    this.pets = pets;
  }

  protected Set<Pet> getPetsInternal() {
    if (this.pets == null) {
      this.pets = new HashSet<Pet>();
    }
    return this.pets;
  }

  /**
   * The pets of this owner.
   *
   * @return The pets of this owner.
   */
  public List<Pet> getPets() {
    return Collections.unmodifiableList((List<Pet>) new ArrayList<Pet>(getPetsInternal()));
  }

  /**
   * The pets of this owner.
   *
   * @param pets The pets of this owner.
   */
  public void setPets(List<Pet> pets) {
    for (Pet pet : pets) {
      addPet(pet);
    }
  }

  /**
   * Add a pet to this owner.
   *
   * @param pet The pet to add.
   */
  public void addPet(Pet pet) {
    getPetsInternal().add(pet);
    pet.setOwner(this);
  }

  /**
   * Get the pet with the given name, or <code>null</code> if none found for this Owner.
   *
   * @param name The name of the pet.
   * @return The pet with the given name.
   */
  public Pet getPet(String name) {
    return getPet(name, false);
  }

  /**
   * Get the pet with the given name, or <code>null</code> if none found for this Owner.
   *
   * @param name      The name of the pet.
   * @param ignoreNew Whether to ignore new pets.
   * @return The pet with the given name.
   */
  public Pet getPet(String name, boolean ignoreNew) {
    name = name.toLowerCase();
    for (Iterator<Pet> it = getPetsInternal().iterator(); it.hasNext();) {
      Pet pet = it.next();
      if (!ignoreNew || !pet.isNew()) {
        String compName = pet.getName();
        compName = compName.toLowerCase();
        if (compName.equals(name)) {
          return pet;
        }
      }
    }
    return null;
  }

}
