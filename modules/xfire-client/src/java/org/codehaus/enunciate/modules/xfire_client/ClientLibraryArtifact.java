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

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.enunciate.main.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * A client-side libarary artifact.
 *
 * @author Ryan Heaton
 */
public class ClientLibraryArtifact extends BaseArtifact implements ArtifactBundle, NamedArtifact {

  private final String name;
  private Date created;
  private String platform;
  private String description;
  private final ArrayList<FileArtifact> artifacts = new ArrayList<FileArtifact>();

  public ClientLibraryArtifact(String module, String id, String name) {
    super(module, id);
    this.name = name;
    this.created = new Date();
  }

  /**
   * If the file to export to is an existing directoy, copy the artifacts to that directory.
   * Otherwise, assume that the export is a file and zip up all the artifacts to that file.
   *
   * @param file The file to write to.
   * @param enunciate The utilities to use.
   */
  public void exportTo(File file, Enunciate enunciate) throws IOException {
    File dir = (file.exists() && file.isDirectory()) ? file : enunciate.createTempDir();
    for (FileArtifact artifact : artifacts) {
      enunciate.copyFile(artifact.getFile(), new File(dir, artifact.getFile().getName()));
    }

    if (!file.exists() || !file.isDirectory()) {
      enunciate.zip(dir, file);
    }
  }

  /**
   * The name for this library.
   *
   * @return The name for this library.
   */
  public String getName() {
    return name;
  }

  /**
   * The artifacts that are associated with this bundle.
   *
   * @return The artifacts that are associated with this bundle.
   */
  public Collection<? extends Artifact> getArtifacts() {
    return this.artifacts;
  }

  /**
   * The artifact to add.
   *
   * @param artifact The artifact to add.
   */
  public void addArtifact(FileArtifact artifact) {
    this.artifacts.add(artifact);
  }

  /**
   * The date this artifact was created (defaults to the date this artifact was constructed).
   *
   * @return The date this artifact was created.
   */
  public Date getCreated() {
    return created;
  }

  /**
   * The date this artifact was created.
   *
   * @param created The date this artifact was created.
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * A string describing the platform this library applies to.
   *
   * @return A string describing the platform this library applies to.
   */
  public String getPlatform() {
    return platform;
  }

  /**
   * A string describing the platform this library applies to.
   *
   * @param platform A string describing the platform this library applies to.
   */
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   * A description of this library.
   *
   * @return A description of this library.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A description of this library.
   *
   * @param description A description of this library.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
