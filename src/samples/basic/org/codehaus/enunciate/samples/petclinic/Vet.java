/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
