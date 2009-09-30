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

package org.codehaus.enunciate.config.war;

/**
 * Configuration element used to specify a pattern of resources to copy.
 *
 * @author Ryan Heaton
 */
public class CopyResources {

  private String pattern;
  private String dir;

  /**
   * The matching pattern for the resources to copy.
   *
   * @return The matching pattern for the resources to copy.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * The matching pattern for the resources to copy.
   *
   * @param pattern The matching pattern for the resources to copy.
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  /**
   * The base directory for the resource copy.
   *
   * @return The base directory for the resource copy.
   */
  public String getDir() {
    return dir;
  }

  /**
   * The base directory for the resource copy.
   *
   * @param dir The base directory for the resource copy.
   */
  public void setDir(String dir) {
    this.dir = dir;
  }
}
