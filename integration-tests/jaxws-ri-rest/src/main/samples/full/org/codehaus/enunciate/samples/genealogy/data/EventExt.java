package org.codehaus.enunciate.samples.genealogy.data;

import java.net.URI;
import java.util.List;

/**
 * Extension for name.
 *
 * @author Ryan Heaton
 */
public class EventExt extends Event {

  private List<URI> links;

  public List<URI> getLinks() {
    return links;
  }

  public void setLinks(List<URI> links) {
    this.links = links;
  }
}
