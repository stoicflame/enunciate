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

package org.codehaus.enunciate.modules.spring_app.config;

import java.io.File;

/**
 * Configuration element used to add spring configuration to the module.
 * 
 * @author Ryan Heaton
 */
public class SpringImport {

  private String file;
  private String Uri;

  /**
   * The spring file to import.
   *
   * @return The spring file to import.
   */
  public String getFile() {
    return file;
  }

  /**
   * The spring file to import.
   *
   * @param file The spring file to import.
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * Used to indicate a URI pointing to the the spring import.
   *
   * @return The URI to the spring import.
   */
  public String getUri() {
    return Uri;
  }

  /**
   * The URI to the spring import.
   *
   * @param uri The URI to the spring import.
   */
  public void setUri(String uri) {
    this.Uri = uri;
  }

}
