/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A generic fact assertion.
 *
 * @author Ryan Heaton
 */
public class Fact extends OccurringAssertion {

  private FactType type;
  private String value;
  private String description;

  /**
   * The fact type.
   *
   * @return The fact type.
   */
  @XmlAttribute
  public FactType getType() {
    return type;
  }

  /**
   * The fact type.
   *
   * @param type The fact type.
   */
  public void setType(FactType type) {
    this.type = type;
  }

  /**
   * The value of the fact.
   *
   * @return The value of the fact.
   */
  public String getValue() {
    return value;
  }

  /**
   * The value of the fact.
   *
   * @param value The value of the fact.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * A description of the fact.
   *
   * @return A description of the fact.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A description of the fact.
   *
   * @param description A description of the fact.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
