package com.ifyouwannabecool.domain.link;

/**
 * A link between two personas.
 *
 * @author Ryan Heaton
 */
public class Link {

  private String persona1Id;
  private String persona2Id;

  /**
   * The id of the first persona.
   *
   * @return The id of the first persona.
   */
  public String getPersona1Id() {
    return persona1Id;
  }

  /**
   * The id of the first persona.
   *
   * @param persona1Id The id of the first persona.
   */
  public void setPersona1Id(String persona1Id) {
    this.persona1Id = persona1Id;
  }

  /**
   * The id of the second persona.
   *
   * @return The id of the second persona.
   */
  public String getPersona2Id() {
    return persona2Id;
  }

  /**
   * The id of the second persona.
   *
   * @param persona2Id The id of the second persona.
   */
  public void setPersona2Id(String persona2Id) {
    this.persona2Id = persona2Id;
  }
  
}
