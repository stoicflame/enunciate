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

import org.joda.time.DateTime;

/**
 * An assertion that occurs at some point in time.
 *
 * @author Ryan Heaton
 */
public abstract class OccurringAssertion extends Assertion {

  private DateTime date;
  private String place;

  /**
   * The date of the occurrence.
   *
   * @return The date of the occurrence.
   */
  public DateTime getDate() {
    return date;
  }

  /**
   * The date of the occurrence.
   *
   * @param date The date of the occurrence.
   */
  public void setDate(DateTime date) {
    this.date = date;
  }

  /**
   * The place of the occurrence.
   *
   * @return The place of the occurrence.
   */
  public String getPlace() {
    return place;
  }

  /**
   * The place of the occurrence.
   *
   * @param place The place of the occurrence.
   */
  public void setPlace(String place) {
    this.place = place;
  }
}
