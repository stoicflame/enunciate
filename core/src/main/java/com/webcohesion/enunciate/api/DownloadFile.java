package com.webcohesion.enunciate.api;

/**
 * @author Ryan Heaton
 */
public class DownloadFile {

  private String name;
  private String size;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
