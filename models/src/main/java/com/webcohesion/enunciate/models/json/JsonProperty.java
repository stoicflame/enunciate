package com.webcohesion.enunciate.models.json;

/**
 * @author Ryan Heaton
 */
public class JsonProperty {

  private String name;
  private JsonTypeDefinition type;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsonTypeDefinition getType() {
    return type;
  }

  public void setType(JsonTypeDefinition type) {
    this.type = type;
  }
}
