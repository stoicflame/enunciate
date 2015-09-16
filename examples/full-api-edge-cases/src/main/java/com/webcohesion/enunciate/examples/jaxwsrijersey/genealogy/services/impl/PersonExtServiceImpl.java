package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Ryan Heaton
 */
@Path ("/personext")
public class PersonExtServiceImpl extends AbstractPersonExtService {

  @GET
  public Response get() {
    return Response.ok().build();
  }

}
