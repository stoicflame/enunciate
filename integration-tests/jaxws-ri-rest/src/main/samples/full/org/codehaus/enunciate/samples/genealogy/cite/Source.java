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

import javax.xml.bind.annotation.*;
import java.net.URI;

/**
 * A source of genealogical information.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Source {

  private String id;
  private String title;
  private URI link;
  private InfoSet[] infoSets;
  private Repository repository;
  private String somethingTransient;

  /**
   * The id of the source.
   *
   * @return The id of the source.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the source.
   *
   * @param id The id of the source.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The title of the source.
   *
   * @return The title of the source.
   */
  public String getTitle() {
    return title;
  }

  /**
   * The title of the source.
   *
   * @param title The title of the source.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * A link to this source.
   *
   * @return A link to this source.
   */
  public URI getLink() {
    return link;
  }

  /**
   * A link to this source.
   *
   * @param link A link to this source.
   */
  public void setLink(URI link) {
    this.link = link;
  }

  /**
   * The infosets associated with this source.
   *
   * @return The infosets associated with this source.
   */
  public InfoSet[] getInfoSets() {
    return infoSets;
  }

  /**
   * The infosets associated with this source.
   *
   * @param infoSets The infosets associated with this source.
   */
  public void setInfoSets(InfoSet[] infoSets) {
    this.infoSets = infoSets;
  }

  /**
   * The repository for this source.
   *
   * @return The repository for this source.
   */
  @XmlElementRef
  public Repository getRepository() {
    return repository;
  }

  /**
   * The repository for this source.
   *
   * @param repository The repository for this source.
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  @XmlTransient
  public String getSomethingTransient() {
    return somethingTransient;
  }

  public void setSomethingTransient(String somethingTransient) {
    this.somethingTransient = somethingTransient;
  }
}
