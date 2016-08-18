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
 * @author Ryan Heaton
 */
public class DependencySourceSpec {

  private String groupId;
  private String artifactId;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public boolean specifies(org.apache.maven.artifact.Artifact artifact) {
    if (this.groupId != null) {
      if (this.artifactId != null) {
        if (this.artifactId.equals(artifact.getArtifactId()) && this.groupId.equals(artifact.getGroupId())) {
          return true;
        }
      }
      else if (this.groupId.equals(artifact.getGroupId())) {
        return true;
      }
    }

    return false;
  }
}
