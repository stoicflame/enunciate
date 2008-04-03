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
package org.codehaus.enunciate.samples.petclinic.services;

import javax.jws.WebService;

import org.codehaus.enunciate.samples.petclinic.Pet;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://org.codehaus.enunciate/samples/petclinic"
)
public interface PetService {

  /**
   * Read an pet.
   *
   * @param id The id of the pet to read.
   * @return The pet.
   * @throws ServiceException If an error occurs while reading the pet.
   */
  Pet readPet(int id) throws ServiceException;

  /**
   * Store an ownder in the database.
   *
   * @param pet The pet of the pet.
   * @throws ServiceException If an error occurs while storing the pet.
   */
  void storePet(Pet pet) throws ServiceException;

}
