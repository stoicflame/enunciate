package org.codehaus.enunciate.samples.json;

import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonRootType;

@JsonName("address")
@JsonRootType
public class Address {
  private String streetAddress;
  private String city;
  private String region;
  private String postalCode;
  private String country;

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }
}
