/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic.services;

import java.util.Collection;

import javax.jws.WebService;

import net.sf.enunciate.samples.petclinic.Owner;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://net.sf.enunciate/samples/petclinic"
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
