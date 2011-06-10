package org.codehaus.enunciate.samples.genealogy.data;

import java.net.URI;
import java.util.List;

/**
 * Extensions for person.
 *
 * @author Ryan Heaton
 */
public class PersonExt extends Person {

  private List<URI> links;

  public List<URI> getLinks() {
    return links;
  }

  public void setLinks(List<URI> links) {
    this.links = links;
  }

}
