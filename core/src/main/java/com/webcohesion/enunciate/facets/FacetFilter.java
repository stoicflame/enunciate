package com.webcohesion.enunciate.facets;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class FacetFilter {

  private final Set<String> includes;
  private final Set<String> excludes;

  public FacetFilter(Set<String> includes, Set<String> excludes) {
    this.includes = includes;
    this.excludes = excludes;
  }

  public boolean accept(HasFacets item) {
    if (item == null) {
      return false;
    }

    if ((includes == null || includes.isEmpty()) && (excludes == null || excludes.isEmpty())) {
      return true;
    }

    boolean accept = true;
    if (includes != null && !includes.isEmpty()) {
      boolean included = false;
      for (Facet facet : item.getFacets()) {
        if (includes.contains(facet.getName())) {
          included = true;
          break;
        }
      }
      accept = included;
    }

    //then remove the items that are explicitly excluded.
    if (excludes != null && !excludes.isEmpty()) {
      for (Facet facet : item.getFacets()) {
        if (excludes.contains(facet.getName())) {
          accept = false;
          break;
        }
      }
    }

    return accept;
  }

}
