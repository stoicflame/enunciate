package com.webcohesion.enunciate.modules.jaxb.model.types;

/**
 * @author Ryan Heaton
 */
public class XmlTypeContext {

  private boolean inArray;
  private boolean inCollection;

  public boolean isInArray() {
    return inArray;
  }

  public void setInArray(boolean inArray) {
    this.inArray = inArray;
  }

  public boolean isInCollection() {
    return inCollection;
  }

  public void setInCollection(boolean inCollection) {
    this.inCollection = inCollection;
  }
}
