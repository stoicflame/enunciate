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
package com.webcohesion.enunciate.mojo;

/**
 * A project artifact.
 *
 * @author Ryan Heaton
 */
public class Artifact {

  private String enunciateArtifactId;
  private String classifier;
  private String artifactType;

  /**
   * The id of the enunciate artifact that is to be a project artifact.
   *
   * @return The id of the enunciate artifact that is to be a project artifact.
   */
  public String getEnunciateArtifactId() {
    return enunciateArtifactId;
  }

  /**
   * The id of the enunciate artifact that is to be a project artifact.
   *
   * @param enunciateArtifactId The id of the enunciate artifact that is to be a project artifact.
   */
  public void setEnunciateArtifactId(String enunciateArtifactId) {
    this.enunciateArtifactId = enunciateArtifactId;
  }

  /**
   * The artifact id.
   *
   * @return The artifact id.
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * The artifact id.
   *
   * @param classifier The artifact id.
   */
  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  /**
   * The packaging of the artifact.
   *
   * @return The packaging of the artifact.
   */
  public String getArtifactType() {
    return artifactType;
  }

  /**
   * The packaging of the artifact.
   *
   * @param artifactType The packaging of the artifact.
   */
  public void setArtifactType(String artifactType) {
    this.artifactType = artifactType;
  }
}
