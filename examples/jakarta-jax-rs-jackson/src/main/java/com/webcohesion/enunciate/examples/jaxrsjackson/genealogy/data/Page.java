package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class Page<E> {

  private List<E> elements;
  private int size;

  public List<E> getElements() {
    return elements;
  }

  public void setElements(List<E> elements) {
    this.elements = elements;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
