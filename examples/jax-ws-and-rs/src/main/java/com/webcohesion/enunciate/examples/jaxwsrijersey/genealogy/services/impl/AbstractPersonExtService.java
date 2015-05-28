package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.impl;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.PersonExt;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Ryan Heaton
 */
public class AbstractPersonExtService {

  @GET
  @Path ("{id}")
  public PersonExt getPersonExt(@PathParam ("id") String id) {
    return new PersonExt();
  }

  @DELETE
  @Path("{id}")
  public void deletePersonExt(@PathParam("id") String id) {

  }
}
