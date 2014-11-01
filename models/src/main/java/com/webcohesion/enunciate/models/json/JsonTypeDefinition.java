package com.webcohesion.enunciate.models.json;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class JsonTypeDefinition {

  private List<JsonProperty> properties;

  public List<JsonProperty> getProperties() {
    return properties;
  }

  public void setProperties(List<JsonProperty> properties) {
    this.properties = properties;
  }
}
