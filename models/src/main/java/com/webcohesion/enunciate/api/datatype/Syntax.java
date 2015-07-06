package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class Syntax {

  private String slug;
  private String label;
  private List<Namespace> namespaces;

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<Namespace> getNamespaces() {
    return namespaces;
  }

}
