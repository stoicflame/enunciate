package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import com.webcohesion.enunciate.metadata.Facet;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * Extensions for person.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
@Facet (name = "http://enunciate.webcohesion.com/samples/full#admin" )
public class PersonAdmin<E extends EventExt> extends Person<E> {

  private List<URI> links;

  public List<URI> getLinks() {
    return links;
  }

  public void setLinks(List<URI> links) {
    this.links = links;
  }

}
