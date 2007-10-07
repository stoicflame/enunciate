/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.config;

/**
 * Specifies already-compiled class(es) that are to be Enunciated.
 *
 * @author Ryan Heaton
 */
public class APIImport {

  private String clazz;
  private boolean seekSource = true;

  /**
   * A specific class to import.
   *
   * @return A specific class to import.
   */
  public String getClassname() {
    return clazz;
  }

  /**
   * A specific class to import.
   *
   * @param clazz A specific class to import.
   */
  public void setClassname(String clazz) {
    this.clazz = clazz;
  }

  /**
   * A specific package to import.  Not supported yet.
   *
   * @param pckg A specific package to import.  Not supported yet.
   */
  public void setPackage(String pckg) {
    throw new UnsupportedOperationException("Adding additional classes by package isn't supported yet.");
  }

  /**
   * Whether to seek the source for these additional classes.
   *
   * @return Whether to seek the source for these additional classes.
   */
  public boolean isSeekSource() {
    return seekSource;
  }

  /**
   * Whether to seek the source for these additional classes.
   *
   * @param seekSource Whether to seek the source for these additional classes.
   */
  public void setSeekSource(boolean seekSource) {
    this.seekSource = seekSource;
  }
}
