package com.webcohesion.enunciate.modules.objc_client;

/**
 * A package-identifier mapping.
 *
 * @author Ryan Heaton
 */
public class PackageIdentifier {

  private String name;
  private String identifier;

  public String getName() {
    return name;
  }

  public void setName(String pckg) {
    this.name = pckg;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
}
