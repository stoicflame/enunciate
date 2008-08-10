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

package org.codehaus.enunciate.modules.xfire_client.config;

/**
 * Configuration specifying the conversion of client-side package names.
 *
 * @author Ryan Heaton
 */
public class ClientPackageConversion {

  private String from;
  private String to;

  /**
   * Regular expression to map from.
   *
   * @return Regular expression to map from.
   */
  public String getFrom() {
    return from;
  }

  /**
   * Regular expression to map from.
   *
   * @param from Regular expression to map from.
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Package to map to.
   *
   * @return Package to map to.
   */
  public String getTo() {
    return to;
  }

  /**
   * Package to map to.
   *
   * @param to Package to map to.
   */
  public void setTo(String to) {
    this.to = to;
  }
}
