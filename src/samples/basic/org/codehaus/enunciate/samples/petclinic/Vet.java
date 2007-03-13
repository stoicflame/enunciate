/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package org.codehaus.enunciate.samples.petclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A veterinarian.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://org.codehaus.enunciate/samples/petclinic/vets"
)
public class Vet extends Person {

  private Set<Specialty> specialties;

  protected void setSpecialtiesInternal(Set<Specialty> specialties) {
    this.specialties = specialties;
  }

  protected Set<Specialty> getSpecialtiesInternal() {
    if (this.specialties == null) {
      this.specialties = new HashSet<Specialty>();
    }
    return this.specialties;
  }

  /**
   * The specialties.
   *
   * @return The specialties.
   */
  public List<Specialty> getSpecialties() {
    return Collections.unmodifiableList((List<Specialty>)new ArrayList<Specialty>(getSpecialtiesInternal()));
  }

  /**
   * The number of specialties.
   *
   * @return The number of specialties.
   */
  public int getSpecialtyCount() {
    return getSpecialtiesInternal().size();
  }

  /**
   * Add a specialty.
   *
   * @param specialty The specialty to add.
   */
  public void addSpecialty(Specialty specialty) {
    getSpecialtiesInternal().add(specialty);
  }

}
