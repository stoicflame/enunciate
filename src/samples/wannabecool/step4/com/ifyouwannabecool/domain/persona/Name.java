package com.ifyouwannabecool.domain.persona;

/**
 * A name of a persona.
 *
 * @author Ryan Heaton
 */
public class Name {

  private String givenName;
  private String surname;

  /**
   * The given name.
   *
   * @return The given name.
   */
  public String getGivenName() {
    return givenName;
  }

  /**
   * The given name.
   *
   * @param givenName The given name.
   */
  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  /**
   * The surname.
   *
   * @return The surname.
   */
  public String getSurname() {
    return surname;
  }

  /**
   * The surname.
   *
   * @param surname The surname.
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }
}
