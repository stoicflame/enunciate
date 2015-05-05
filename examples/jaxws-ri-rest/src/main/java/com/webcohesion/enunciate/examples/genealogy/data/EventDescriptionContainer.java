package com.webcohesion.enunciate.examples.genealogy.data;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class EventDescriptionContainer {

  private List<Event> typesToDescriptions;

  public List<Event> getTypesToDescriptions() {
    return typesToDescriptions;
  }

  public void setTypesToDescriptions(List<Event> typesToDescriptions) {
    this.typesToDescriptions = typesToDescriptions;
  }
}
