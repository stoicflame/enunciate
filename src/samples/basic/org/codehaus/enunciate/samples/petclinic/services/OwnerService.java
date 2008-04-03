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

import java.util.Collection;

import javax.jws.WebService;

import org.codehaus.enunciate.samples.petclinic.Owner;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://org.codehaus.enunciate/samples/petclinic"
)
public interface OwnerService {

  /**
   * Find all owners of the given last name.
   *
   * @param lastName The last name of the owner.
   * @return The owners of the given last name.
   * @throws ServiceException If an error occurs while finding or reading the owners.
   */
  Collection<Owner> findOwners(String lastName) throws ServiceException;

  /**
   * Read an owner.
   *
   * @param id The id of the owner to read.
   * @return The owner.
   * @throws ServiceException If an error occurs while reading the owner.
   */
  Owner readOwner(int id) throws ServiceException;

  /**
   * Store an ownder in the database.
   *
   * @param owner The owner of the pet.
   * @throws ServiceException If an error occurs while storing the owner.
   */
  void storeOwner(Owner owner) throws ServiceException;
}
