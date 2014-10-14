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

package org.codehaus.enunciate.config;

/**
 * Specifies already-compiled class(es) that are to be Enunciated.
 *
 * @author Ryan Heaton
 */
public class APIImport {

  private boolean seekSource = true;
  private String pattern;

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

  /**
   * The pattern (dot-delimited, ant-style) of classes to import.
   *
   * @return The pattern (dot-delimited, ant-style) of classes to import.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * The pattern (dot-delimited, ant-style) of classes to import.
   *
   * @param pattern The pattern (dot-delimited, ant-style) of classes to import.
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
