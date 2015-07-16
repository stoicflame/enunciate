package com.webcohesion.enunciate.modules.jaxrs.api.impl;

/**
 * @author Ryan Heaton
 */
public enum KnownFacet {

  resource_class("http://enunciate.webcohesion.com/facets#resource-class"),

  resource_path("http://enunciate.webcohesion.com/facets#resource-path"),

  other("");


  private final String value;

  KnownFacet(String facet) {
    this.value = facet;
  }

  public String getValue() {
    return value;
  }

  public static KnownFacet fromString(String facet) {
    for (KnownFacet knownFacet : values()) {
      if (knownFacet.getValue().equals(facet)) {
        return knownFacet;
      }
    }

    return other;
  }
}
