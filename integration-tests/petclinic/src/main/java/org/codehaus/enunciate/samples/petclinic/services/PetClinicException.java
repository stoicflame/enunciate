package org.codehaus.enunciate.samples.petclinic.services;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault
public class PetClinicException extends Exception {

  public PetClinicException(String message) {
    super(message);
  }
}
