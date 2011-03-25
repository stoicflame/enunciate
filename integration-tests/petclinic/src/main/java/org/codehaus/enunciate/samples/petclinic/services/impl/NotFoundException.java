package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.samples.petclinic.services.PetClinicException;

/**
 * @author Ryan Heaton
 */
public class NotFoundException extends PetClinicException {

  public NotFoundException(String message) {
    super(message);
  }
}
