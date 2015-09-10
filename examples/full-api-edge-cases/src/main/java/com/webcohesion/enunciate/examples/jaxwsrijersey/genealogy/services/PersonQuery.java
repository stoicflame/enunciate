package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import javax.ws.rs.QueryParam;

/**
 * @author Ryan Heaton
 */
public class PersonQuery {

  /**
   * The name of the person.
   */
  @QueryParam("name")
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
