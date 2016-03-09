package com.webcohesion.enunciate.artifacts;

/**
 * @author Ryan Heaton
 */
public class ClientLibraryJavaArtifact extends ClientLibraryArtifact {

  private String groupId;
  private String artifactId;
  private String version;

  public ClientLibraryJavaArtifact(String module, String id, String name) {
    super(module, id, name);
  }

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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
