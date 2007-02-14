package com.ifyouwannabecool.domain.persona;

import javax.activation.DataHandler;

/**
 * @author Ryan Heaton
 */
public class Persona {

  private String id;
  private String email;
  private String alias;
  private Name name;
  private javax.activation.DataHandler picture;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

  public DataHandler getPicture() {
    return picture;
  }

  public void setPicture(DataHandler picture) {
    this.picture = picture;
  }
}
