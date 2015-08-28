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
