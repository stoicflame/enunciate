/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.metadata.Label;

import java.util.Collection;
import java.util.Map;

/**
 * A person.  The central data in genealogical information.
 *
 * @author Ryan Heaton
 */
@JsonSerialize
public class Person<EV extends Event> {

  private String id;
  private Gender gender;
  private Collection<? extends Name> names;
  private Collection<EV> events;
  private Collection<? extends Fact> facts;
  private Collection<? extends Relationship> relationships;
  private Map<EventType, String> eventDescriptions;
  private Assertion primaryAssertion;
  private State state;
  private MapThing mapThing;

  @Label ("PersonState")
  enum State {

    old,

    young

  }

  /**
   * The person id.
   *
   * @return The person id.
   */
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

  public Assertion getPrimaryAssertion() {
    return primaryAssertion;
  }

  public void setPrimaryAssertion(Assertion primaryAssertion) {
    this.primaryAssertion = primaryAssertion;
  }

  /**
   * The names of the person.
   *
   * @return The names of the person.
   */
  public Collection<? extends Name> getNames() {
    return names;
  }

  /**
   * The names of the person.
   *
   * @param names The names of the person.
   */
  public void setNames(Collection<? extends Name> names) {
    this.names = names;
  }

  /**
   * The events associated with a person.
   *
   * @return The events associated with a person.
   */
  public Collection<EV> getEvents() {
    return events;
  }

  /**
   * The events associated with a person.
   *
   * @param events The events associated with a person.
   */
  public void setEvents(Collection<EV> events) {
    this.events = events;
  }

  /**
   * The facts about a person.
   *
   * @return The facts about a person.
   */
  public Collection<? extends Fact> getFacts() {
    return facts;
  }

  /**
   * The facts about a person.
   *
   * @param facts The facts about a person.
   */
  public void setFacts(Collection<? extends Fact> facts) {
    this.facts = facts;
  }

  /**
   * The relationships of a person.
   *
   * @return The relationships of a person.
   */
  public Collection<? extends Relationship> getRelationships() {
    return relationships;
  }

  /**
   * The relationships of a person.
   *
   * @param relationships The relationships of a person.
   */
  public void setRelationships(Collection<? extends Relationship> relationships) {
    this.relationships = relationships;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public MapThing getMapThing() {
    return mapThing;
  }

  public void setMapThing(MapThing mapThing) {
    this.mapThing = mapThing;
  }
}
