/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package net.sf.enunciate.samples.petclinic.services;

import java.util.Collection;

import javax.jws.WebService;

import net.sf.enunciate.samples.petclinic.Vet;
import net.sf.enunciate.samples.petclinic.Pet;

/**
 * Service for working with vets.
 *
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://net.sf.enunciate/samples/petclinic"
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
