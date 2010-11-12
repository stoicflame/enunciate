package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.rest.annotations.RESTError;
import org.codehaus.enunciate.samples.petclinic.services.PetClinicException;

/**
 * @author Ryan Heaton
 */
@RESTError (
  errorCode = 404
)
public class NotFoundException extends PetClinicException {

  public NotFoundException(String message) {
    super(message);
  }
}
