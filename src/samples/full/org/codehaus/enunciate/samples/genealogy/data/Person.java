package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.activation.DataHandler;
import java.util.Collection;

/**
 * A person.  The central data in genealogical information.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Person {

  private String id;
  private Gender gender;
  private Collection<Name> names;
  private Collection<Event> events;
  private Collection<Fact> facts;
  private Collection<Relationship> relationships;

  private DataHandler picture;

  /**
   * The person id.
   *
   * @return The person id.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The person id.
   *
   * @param id The person id.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The gender of a person.
   *
   * @return The gender of a person.
   */
  public Gender getGender() {
    return gender;
  }

  /**
   * The gender of a person.
   *
   * @param gender The gender of a person.
   */
  public void setGender(Gender gender) {
    this.gender = gender;
  }

  /**
   * The names of the person.
   *
   * @return The names of the person.
   */
  public Collection<Name> getNames() {
    return names;
  }

  /**
   * The names of the person.
   *
   * @param names The names of the person.
   */
  public void setNames(Collection<Name> names) {
    this.names = names;
  }

  /**
   * The events associated with a person.
   *
   * @return The events associated with a person.
   */
  public Collection<Event> getEvents() {
    return events;
  }

  /**
   * The events associated with a person.
   *
   * @param events The events associated with a person.
   */
  public void setEvents(Collection<Event> events) {
    this.events = events;
  }

  /**
   * The facts about a person.
   *
   * @return The facts about a person.
   */
  public Collection<Fact> getFacts() {
    return facts;
  }

  /**
   * The facts about a person.
   *
   * @param facts The facts about a person.
   */
  public void setFacts(Collection<Fact> facts) {
    this.facts = facts;
  }

  /**
   * The relationships of a person.
   *
   * @return The relationships of a person.
   */
  public Collection<Relationship> getRelationships() {
    return relationships;
  }

  /**
   * The relationships of a person.
   *
   * @param relationships The relationships of a person.
   */
  public void setRelationships(Collection<Relationship> relationships) {
    this.relationships = relationships;
  }

  /**
   * A picture of a person.
   *
   * @return A picture of a person.
   */
  public DataHandler getPicture() {
    return picture;
  }

  /**
   * A picture of a person.
   *
   * @param picture A picture of a person.
   */
  public void setPicture(DataHandler picture) {
    this.picture = picture;
  }
}
