package com.webcohesion.enunciate.examples.jboss.genealogy.services.impl;

import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;

/**
 * @author Ryan Heaton
 */
public class PersonForm {

  /**
   * The name of the person
   */
  @FormParam("name")
  public String name;

  private String gender;

  /**
   * The gender of the person.
   *
   * @return The gender of the person.
   */
  public String getGender() {
    return gender;
  }

  /**
   * The gender of the person.
   *
   * @param gender The gender of the person.
   */
  @QueryParam("gender")
  public void setGender(String gender) {
    this.gender = gender;
  }
}
