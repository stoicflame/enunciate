/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.cite;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.Assertion;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * A set of information.
 *
 * @author Ryan Heaton
 */
public class InfoSet {

  private String id;
  private Contributor submitter;
  private Source source;
  private String sourceReference;

  /**
   * The id of the infoset.
   *
   * @return The id of the infoset.
   */
  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  /**
   * The id of the infoset.
   *
   * @param id The id of the infoset.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The submitter of the information.
   *
   * @return The submitter of the information.
   */
  @XmlElementRef
  public Contributor getSubmitter() {
    return submitter;
  }

  /**
   * The submitter of the information.
   *
   * @param submitter The submitter of the information.
   */
  public void setSubmitter(Contributor submitter) {
    this.submitter = submitter;
  }

  /**
   * The source of the information.
   *
   * @return The source of the information.
   */
  @XmlIDREF
  public Source getSource() {
    return source;
  }

  /**
   * The source of the information.
   *
   * @param source The source of the information.
   */
  public void setSource(Source source) {
    this.source = source;
  }

  /**
   * A reference within the source of this information.
   *
   * @return A reference within the source of this information.
   */
  public String getSourceReference() {
    return sourceReference;
  }

  /**
   * A reference within the source of this information.
   *
   * @param sourceReference A reference within the source of this information.
   */
  public void setSourceReference(String sourceReference) {
    this.sourceReference = sourceReference;
  }
}
