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

package org.codehaus.enunciate.modules.spring_app.config;

/**
 * Configuration object for specifying a war library.
 *
 * @author Ryan Heaton
 */
public class WarLib {

  private String path;

  /**
   * The path to the library to be added to the war.
   *
   * @return The path to the library to be added to the war.
   */
  public String getPath() {
    return path;
  }

  /**
   * The path to the library to be added to the war.
   *
   * @param path The path to the library to be added to the war.
   */
  public void setPath(String path) {
    this.path = path;
  }
}
