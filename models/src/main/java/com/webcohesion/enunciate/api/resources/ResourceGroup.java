package com.webcohesion.enunciate.api.resources;

import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResourceGroup {

  private String slug;
  private String label;
  private String description;
  private String deprecated;
  private Set<String> methods;
  private List<? extends Resource> resources;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDeprecated() {
    return deprecated;
  }

  public void setDeprecated(String deprecated) {
    this.deprecated = deprecated;
  }

  public Set<String> getMethods() {
    return methods;
  }

  public void setMethods(Set<String> methods) {
    this.methods = methods;
  }

  public List<? extends Resource> getResources() {
    return resources;
  }

  public void setResources(List<? extends Resource> resources) {
    this.resources = resources;
  }
}
