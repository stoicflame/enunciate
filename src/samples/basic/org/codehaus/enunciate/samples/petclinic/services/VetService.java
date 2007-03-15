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
package org.codehaus.enunciate.samples.petclinic.services;

import java.util.Collection;

import javax.jws.WebService;

import org.codehaus.enunciate.samples.petclinic.Vet;
import org.codehaus.enunciate.samples.petclinic.Pet;

/**
 * Service for working with vets.
 *
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://org.codehaus.enunciate/samples/petclinic"
)
public interface VetService {

  /**
   * Get the collection of vets.
   *
   * @return The vets.
   * @throws ServiceException If an error occurs while reading the vets.
   */
  Collection<Vet> getVets() throws ServiceException;

  /**
   * Store a vet.
   *
   * @param vet The vet to store.
   * @throws ServiceException If an error occurs while storing the given vet.
   */
  void storeVet(Vet vet) throws ServiceException;

  /**
   * Record a visit.
   *
   * @param vet The vet who visited.
   * @param pet The pet that was visited.
   * @return Whether a new visit was recorded.
   * @throws ServiceException If an error occurs recording the visit.
   */
  boolean recordVisit(Vet vet, Pet pet, String description) throws ServiceException;

}
