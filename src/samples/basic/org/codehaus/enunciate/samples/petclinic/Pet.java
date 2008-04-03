/*
 * Copyright 2006-2008 Web Cohesion
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A pet.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://org.codehaus.enunciate/samples/petclinic/pets"
)
public class Pet extends NamedEntity {

  private Date birthDate;
  private PetType type;
  private Owner owner;
  private Set<Visit> visits;

  /**
   * The birthdate of the pet.
   *
   * @param birthDate The birthdate of the pet.
   */
  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  /**
   * The birthdate of the pet.
   *
   * @return The birthdate of the pet.
   */
  public Date getBirthDate() {
    return this.birthDate;
  }

  /**
   * The type of pet.
   *
   * @param type The type of pet.
   */
  public void setType(PetType type) {
    this.type = type;
  }

  /**
   * The type of pet.
   *
   * @return The type of pet.
   */
  public PetType getType() {
    return type;
  }

  /**
   * The owner.
   *
   * @param owner The owner.
   */
  protected void setOwner(Owner owner) {
    this.owner = owner;
  }

  /**
   * The owner.
   *
   * @return The owner.
   */
  public Owner getOwner() {
    return owner;
  }

  protected void setVisitsInternal(Set<Visit> visits) {
    this.visits = visits;
  }

  protected Set<Visit> getVisitsInternal() {
    if (this.visits == null) {
      this.visits = new HashSet<Visit>();
    }
    return this.visits;
  }

  /**
   * The visits of this pet.
   *
   * @return The visits of this pet.
   */
  public List<Visit> getVisits() {
    return Collections.unmodifiableList(new ArrayList<Visit>(getVisitsInternal()));
  }

  /**
   * Add a visit to this pet.
   *
   * @param visit The visit to add.
   */
  public void addVisit(Visit visit) {
    getVisitsInternal().add(visit);
    visit.setPet(this);
  }

}
