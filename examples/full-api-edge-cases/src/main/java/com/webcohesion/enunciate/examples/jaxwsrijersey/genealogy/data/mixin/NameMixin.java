package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.mixin;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Ryan Heaton
 */
public abstract class NameMixin {

  /**
   * The title of the name.
   *
   * @return The title of the name.
   */
  @JsonProperty("title")
  abstract String getPrefix();

  /**
   * The title of the name.
   *
   * @param prefix The title of the name.
   */
  abstract void setPrefix(String prefix);
}
