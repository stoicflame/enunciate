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
 * A gender assertion.
 *
 * @author Ryan Heaton
 */
public class Gender extends Assertion {

  private GenderType type;

  /**
   * The type of gender.
   *
   * @return The type of gender.
   */
  @XmlAttribute
  public GenderType getType() {
    return type;
  }

  /**
   * The type of gender.
   *
   * @param type The type of gender.
   */
  public void setType(GenderType type) {
    this.type = type;
  }
}
