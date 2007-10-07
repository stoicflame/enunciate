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
 * Configuration element used to specify a pattern of jars to exclude from the war.
 *
 * @author Ryan Heaton
 */
public class IncludeExcludeLibs {

  private File file;
  private String pattern;
  private boolean includeInManifest = false;

  /**
   * The file to exclude.
   *
   * @return The file to exclude.
   */
  public File getFile() {
    return file;
  }

  /**
   * The file to exclude.
   *
   * @param file The file to exclude.
   */
  public void setFile(File file) {
    this.file = file;
  }

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
   * Whether to include the jar in the manifest.
   *
   * @return Whether to include the jar in the manifest.
   */
  public boolean isIncludeInManifest() {
    return includeInManifest;
  }

  /**
   * Whether to include the jar in the manifest.
   *
   * @param includeInManifest Whether to include the jar in the manifest.
   */
  public void setIncludeInManifest(boolean includeInManifest) {
    this.includeInManifest = includeInManifest;
  }
}
