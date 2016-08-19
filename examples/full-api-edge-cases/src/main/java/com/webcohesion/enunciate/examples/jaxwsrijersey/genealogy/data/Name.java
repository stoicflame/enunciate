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
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A name assertion. An example name is Yamada Tarō (山田太郎).
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Name extends Assertion {

  private String value;
  private Character middleInitial;
  private String prefix;

  /**
   * The text value of the name.
   *
   * @return The text value of the name.
   */
  public String getValue() {
    return value;
  }

  /**
   * The text value of the name.
   *
   * @param value The text value of the name.
   */
  public void setValue(String value) {
    this.value = value;
  }

  public Character getMiddleInitial() {
    return middleInitial;
  }

  public void setMiddleInitial(Character middleInitial) {
    this.middleInitial = middleInitial;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
