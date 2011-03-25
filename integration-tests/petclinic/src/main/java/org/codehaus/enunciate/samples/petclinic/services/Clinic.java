package org.codehaus.enunciate.samples.petclinic.services;

import org.codehaus.enunciate.samples.petclinic.schema.*;
import org.codehaus.enunciate.modules.gwt.GWTTransient;

import javax.jws.WebService;
import javax.activation.DataHandler;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import java.util.Collection;

/**
 * The high-level PetClinic business interface.
 * <p/>
 * <p>This is basically a data access object,
 * as PetClinic doesn't have dedicated business logic.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@WebService (
  targetNamespace = "http://enunciate.codehaus.org/services",
  serviceName = "clinic"
)
@PermitAll
public interface Clinic {

  /**
   * Get a photo of a vet by id.
   *
   * @param id The vet id.
   * @return The photo (raw data) of the vet.
   */
  @Path("/vetpix/{id}")
  @GET
  @GWTTransient
  @RolesAllowed( "ADMIN" )
  DataHandler getVetPhoto(@PathParam ("id") Integer id) throws PetClinicException, PictureException;

  /**
   * Sets a specific vet photo.
   *
   * @param dataHandler The data handler for the photo.
   * @param id The id of the vet for which to store a picture.
   */
  @Path("/vetpix/{id}")
  @PUT
  @GWTTransient
  void storeVetPhoto(DataHandler dataHandler, @PathParam("id") Integer id) throws PetClinicException, PictureException;

  /**
   * Retrieve all <code>Vet</code>s from the datastore.
   *
   * @return a <code>Collection</code> of <code>Vet</code>s
   */
  Collection<Vet> getVets() throws PetClinicException;

  /**
   * Retrieve all <code>PetType</code>s from the datastore.
   *
   * @return a <code>Collection</code> of <code>PetType</code>s
   */
  Collection<PetType> getPetTypes() throws PetClinicException;

  /**
   * Retrieve <code>Owner</code>s from the datastore by last name,
   * returning all owners whose last name <i>starts</i> with the given name.
   *
   * @param lastName Value to search for
   * @return a <code>Collection</code> of matching <code>Owner</code>s
   *         (or an empty <code>Collection</code> if none found)
   */
  Collection<Owner> findOwners(String lastName) throws PetClinicException;

  /**
   * Retrieve an <code>Owner</code> from the datastore by id.
   *
   * @param id the id to search for
   * @return the <code>Owner</code> if found
   */
  @Path ("/owner/{id}")
  @GET
  Owner loadOwner(@PathParam ("id") int id) throws PetClinicException;

  /**
   * Retrieve a <code>Pet</code> from the datastore by id.
   *
   * @param id the id to search for
   * @return the <code>Pet</code> if found
   */
  Pet loadPet(int id) throws PetClinicException;

  /**
   * Save an <code>Owner</code> to the datastore,
   * either inserting or updating it.
   *
   * @param owner the <code>Owner</code> to save
   * @see org.codehaus.enunciate.samples.petclinic.schema.Entity#isNew
   */
  void storeOwner(Owner owner) throws PetClinicException;

  /**
   * Save a <code>Pet</code> to the datastore,
   * either inserting or updating it.
   *
   * @param pet the <code>Pet</code> to save
   * @see org.codehaus.enunciate.samples.petclinic.schema.Entity#isNew
   */
  void storePet(Pet pet) throws PetClinicException;

  /**
   * Save a <code>Visit</code> to the datastore,
   * either inserting or updating it.
   *
   * @param visit the <code>Visit</code> to save
   * @see org.codehaus.enunciate.samples.petclinic.schema.Entity#isNew
   */
  void storeVisit(Visit visit) throws PetClinicException;

}
