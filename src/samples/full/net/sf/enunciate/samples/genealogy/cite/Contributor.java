package net.sf.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

/**
 * A contributor of information from a source.
 * 
 * @author Ryan Heaton
 */
@XmlRootElement
public class Contributor {

  private String id;
  private String contactName;
  private Collection<EMail> emails;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  @XmlList
  public Collection<EMail> getEmails() {
    return emails;
  }

  public void setEmails(Collection<EMail> emails) {
    this.emails = emails;
  }
}
