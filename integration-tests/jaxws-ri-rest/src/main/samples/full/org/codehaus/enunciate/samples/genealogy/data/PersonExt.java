package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * Extensions for person.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class PersonExt<E extends EventExt> extends Person<E> {

  private List<URI> links;

  public List<URI> getLinks() {
    return links;
  }

  public void setLinks(List<URI> links) {
    this.links = links;
  }

}
