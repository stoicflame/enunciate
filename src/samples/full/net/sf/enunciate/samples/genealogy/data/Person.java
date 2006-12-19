package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.activation.DataHandler;
import java.util.Collection;

/**
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

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Collection<Name> getNames() {
    return names;
  }

  public void setNames(Collection<Name> names) {
    this.names = names;
  }

  public Collection<Event> getEvents() {
    return events;
  }

  public void setEvents(Collection<Event> events) {
    this.events = events;
  }

  public Collection<Fact> getFacts() {
    return facts;
  }

  public void setFacts(Collection<Fact> facts) {
    this.facts = facts;
  }

  public Collection<Relationship> getRelationships() {
    return relationships;
  }

  public void setRelationships(Collection<Relationship> relationships) {
    this.relationships = relationships;
  }

  public DataHandler getPicture() {
    return picture;
  }

  public void setPicture(DataHandler picture) {
    this.picture = picture;
  }
}
