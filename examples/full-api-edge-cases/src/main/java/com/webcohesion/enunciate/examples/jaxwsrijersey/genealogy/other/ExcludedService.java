package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.other;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Ryan Heaton
 */
@Path("/should/be/excluded")
public class ExcludedService {

  @GET
  public String get() {
    return "excluded";
  }

}
