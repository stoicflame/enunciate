package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.PersonAdmin;
import com.webcohesion.enunciate.metadata.Facet;

import javax.jws.WebService;

/**
 * This is the admin service
 */
@Facet ( "http://enunciate.webcohesion.com/samples/full#admin" )
@WebService ( targetNamespace = "http://enunciate.webcohesion.com/samples/full" )
public interface AdminService {

  /**
   * This is the doc for read admin person.
   *
   * @param id The id.
   * @return The admin person.
   */
  PersonAdmin readAdminPerson(String id);
}
