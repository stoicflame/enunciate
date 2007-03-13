package org.codehaus.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A repository for sources.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Repository {

  private String id;
  private String location;
  private EMail email;

  /**
   * The id of the repository.
   *
   * @return The id of the repository.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the repository.
   *
   * @param id The id of the repository.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The location for this repository.
   *
   * @return The location for this repository.
   */
  public String getLocation() {
    return location;
  }

  /**
   * The location for this repository.
   *
   * @param location The location for this repository.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * An e-mail address for this repository.
   *
   * @return An e-mail address for this repository.
   */
  public EMail getEmail() {
    return email;
  }

  /**
   * An e-mail address for this repository.
   *
   * @param email An e-mail address for this repository.
   */
  public void setEmail(EMail email) {
    this.email = email;
  }
}
