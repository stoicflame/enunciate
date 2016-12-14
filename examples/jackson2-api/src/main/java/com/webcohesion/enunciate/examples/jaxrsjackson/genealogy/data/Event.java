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
package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An event assertion.
 *
 * @author Ryan Heaton
 */
public class Event extends OccurringAssertion {

  private EventType type;
  private String description;
  private final List<String> tags = new ArrayList<String>();
  private String explanation;

  /**
   * The type of this event.
   *
   * @return The type of this event.
   */
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
  @NotNull
  @Size (min = 1, max = 255)
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

  public String[] getTags() {
    return tags.toArray(new String[tags.size()]);
  }

  public void setTags(String tags[]) {
    this.tags.clear();
    this.tags.addAll(Arrays.asList(tags));
  }

  @NotNull
  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

}
