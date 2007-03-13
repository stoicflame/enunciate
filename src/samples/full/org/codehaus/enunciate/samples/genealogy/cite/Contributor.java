package org.codehaus.enunciate.samples.genealogy.cite;

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

  /**
   * The id of the contributor.
   *
   * @return The id of the contributor.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the contributor.
   *
   * @param id The id of the contributor.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The contact name for the contributor.
   *
   * @return The contact name for the contributor.
   */
  public String getContactName() {
    return contactName;
  }

  /**
   * The contact name for the contributor.
   *
   * @param contactName The contact name for the contributor.
   */
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /**
   * The email addresses associated with this contributor.
   *
   * @return The email addresses associated with this contributor.
   */
  @XmlList
  public Collection<EMail> getEmails() {
    return emails;
  }

  /**
   * The email addresses associated with this contributor.
   *
   * @param emails The email addresses associated with this contributor.
   */
  public void setEmails(Collection<EMail> emails) {
    this.emails = emails;
  }
}
