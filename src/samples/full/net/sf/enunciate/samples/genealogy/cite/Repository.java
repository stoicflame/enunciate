package net.sf.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Repository {

  private String id;
  private String location;
  private EMail email;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public EMail getEmail() {
    return email;
  }

  public void setEmail(EMail email) {
    this.email = email;
  }
}
