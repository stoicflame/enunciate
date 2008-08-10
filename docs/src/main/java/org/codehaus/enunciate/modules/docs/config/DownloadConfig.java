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

package org.codehaus.enunciate.modules.docs.config;

import java.io.File;

/**
 * Configuration for a download.
 * 
 * @author Ryan Heaton
 */
public class DownloadConfig {

  private String artifact;
  private String name;
  private String description;
  private String file;

  /**
   * The artifact to expose as a download.
   *
   * @return The artifact to expose as a download.
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * The artifact to expose as a download.
   *
   * @param artifact The artifact to expose as a download.
   */
  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  /**
   * The name of the download.
   *
   * @return The name of the download.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the download.
   *
   * @param name The name of the download.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The description of the download. (Ignored if "artifact" is set.)
   *
   * @return The description of the download. (Ignored if "artifact" is set.)
   */
  public String getDescription() {
    return description;
  }

  /**
   * The description of the download. (Ignored if "artifact" is set.)
   *
   * @param description The description of the download. (Ignored if "artifact" is set.)
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The file to expose as a download. (Ignored if "artifact" is set.)
   *
   * @return The file to expose as a download. (Ignored if "artifact" is set.)
   */
  public String getFile() {
    return file;
  }

  /**
   * The file to expose as a download. (Ignored if "artifact" is set.)
   *
   * @param file The file to expose as a download. (Ignored if "artifact" is set.)
   */
  public void setFile(String file) {
    this.file = file;
  }
}
