/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

/**
 * A contributor of information from a source.<br>
 * With some invalid XML in the documentation...
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
