/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.samples.idl.genealogy.data;

import com.webcohesion.enunciate.samples.idl.genealogy.cite.Note;

import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.activation.DataHandler;
import java.util.Collection;
import java.util.Map;

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
  private Map<String, Note> notes;

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

  /**
   * A map of contributor ids to notes.
   *
   * @return A map of contributor ids to notes.
   */
  public Map<String, Note> getNotes() {
    return notes;
  }

  /**
   * A map of contributor ids to notes.
   *
   * @param notes A map of contributor ids to notes.
   */
  public void setNotes(Map<String, Note> notes) {
    this.notes = notes;
  }
}
