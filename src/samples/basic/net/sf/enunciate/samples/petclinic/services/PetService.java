/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic.services;

import javax.jws.WebService;

import net.sf.enunciate.samples.petclinic.Pet;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://net.sf.enunciate/samples/petclinic"
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
