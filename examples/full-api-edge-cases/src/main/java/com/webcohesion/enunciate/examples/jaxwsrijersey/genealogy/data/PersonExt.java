package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import com.webcohesion.enunciate.metadata.Facet;

import javax.xml.bind.annotation.XmlAttribute;
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

  private String adminAcls;
  private String adminPrivacy;
  private List<URI> links;

  public List<URI> getLinks() {
    return links;
  }

  public void setLinks(List<URI> links) {
    this.links = links;
  }

  @Facet (name = "http://enunciate.webcohesion.com/samples/full#admin" )
  public String getAdminAcls() {
    return adminAcls;
  }

  public void setAdminAcls(String adminAcls) {
    this.adminAcls = adminAcls;
  }

  @XmlAttribute
  @Facet (name = "http://enunciate.webcohesion.com/samples/full#admin" )
  public String getAdminPrivacy() {
    return adminPrivacy;
  }

  public void setAdminPrivacy(String adminPrivacy) {
    this.adminPrivacy = adminPrivacy;
  }
}
