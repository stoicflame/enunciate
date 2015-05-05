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

package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * An event assertion.
 *
 * @author Ryan Heaton
 */
public class Event extends OccurringAssertion {

  private EventType type;
  private String description;

  /**
   * The type of this event.
   *
   * @return The type of this event.
   */
  @XmlAttribute
  public EventType getType() {
    return type;
  }

  /**
   * The type of this event.
   *
   * @param type The type of this event.
   */
  public void setType(EventType type) {
    this.type = type;
  }

  /**
   * A description of this event.
   *
   * @return A description of this event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A description of this event.
   *
   * @param description A description of this event.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
