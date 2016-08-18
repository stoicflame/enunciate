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
package com.webcohesion.enunciate.api.datatype;

/**
 * @author Ryan Heaton
 */
public class PropertyMetadata {

  private final boolean structure;
  private final String value;
  private final String title;
  private final String href;

  public PropertyMetadata(String value, String title, String href) {
    this.value = value;
    this.title = title;
    this.href = href;
    this.structure = true;
  }

  public PropertyMetadata(String value) {
    this.value = value;
    this.title = null;
    this.href = null;
    this.structure = false;
  }

  public final boolean isStructure() {
    return this.structure;
  }

  public String getValue() {
    return value;
  }

  public String getTitle() {
    return title;
  }

  public String getHref() {
    return href;
  }

  @Override
  public String toString() {
    return getValue();
  }
}
