package com.webcohesion.enunciate.models.facets;

/**
 * Used to declare a "facet" for the sake of grouping resources and APIs together for simpler browsing.
 *
 * @author Ryan Heaton
 */
public class Facet implements Comparable<Facet> {

  private final String name;
  private final String value;
  private final String documentation;

  public Facet(String name, String value) {
    this(name, value, null);
  }

  public Facet(String name, String value, String documentation) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;

    if (value == null) {
      throw new NullPointerException();
    }
    this.value = value;
    this.documentation = documentation;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getDocumentation() {
    return documentation;
  }

  @Override
  public int compareTo(Facet o) {
    String comparison1 = this.name + this.value;
    String comparison2 = o.name + o.value;
    return comparison1.compareTo(comparison2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Facet facet = (Facet) o;

    if (!name.equals(facet.name)) {
      return false;
    }
    if (!value.equals(facet.value)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

}
