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

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A visit of a pet.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://org.codehaus.enunciate/samples/petclinic/vets"
)
public class Visit extends Entity {

  private Date date;
  private String description;
  private Pet pet;

  /**
   * Creates a new instance of Visit for the current date
   */
  public Visit() {
    this.date = new Date();
  }

  /**
   * The date.
   *
   * @return The date.
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * The date.
   *
   * @param date The date.
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * The description of the visit.
   *
   * @return The description of the visit.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * The description of the visit.
   *
   * @param description The description of the visit.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The pet for the visit.
   *
   * @return The pet for the visit.
   */
  public Pet getPet() {
    return this.pet;
  }

  /**
   * The pet for the visit.
   *
   * @param pet The pet for the visit.
   */
  protected void setPet(Pet pet) {
    this.pet = pet;
  }

}
