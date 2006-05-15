package net.sf.enunciate;

import java.net.URL;

/**
 * The set of APIs supported.
 *
 * @author Ryan Heaton
 */
public enum API {

  XML("template/xml.fmt");

  private String resource;

  API(String resource) {
    this.resource = resource;
  }

  /**
   * The script to generate this API.
   * 
   * @return The script to generate this API.
   */
  public URL getTemplate() {
    return getClass().getResource(resource);
  }

}
