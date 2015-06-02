package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data.api;

import com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Ryan Heaton
 */
@Path("/persons")
public class PersonService {

  @GET
  @Path("{id}")
  public Person getPerson(@PathParam("id") String id) {
    return new Person();
  }

}
