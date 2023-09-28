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

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An event assertion.
 *
 * @author Ryan Heaton
 */
public class Event extends OccurringAssertion {

  @Getter
  @Setter
  private EventType type;
  @Setter
  private String description;
  private final List<String> tags = new ArrayList<String>();
  @Getter
  private String explanation;

  /**
   * A description of this event.
   *
   * @return A description of this event.
   */
  @NotNull
  public String getDescription() {
    return description;
  }

  public String[] getTags() {
    return tags.toArray(new String[tags.size()]);
  }

  public void setTags(String tags[]) {
    this.tags.clear();
    this.tags.addAll(Arrays.asList(tags));
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

}
