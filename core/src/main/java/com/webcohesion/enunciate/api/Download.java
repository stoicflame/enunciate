package com.webcohesion.enunciate.api;

import java.util.Date;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class Download {

  private String slug;
  private String name;
  private String description;
  private Date created;
  private List<DownloadFile> files;
  private String groupId;
  private String artifactId;
  private String version;

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public List<DownloadFile> getFiles() {
    return files;
  }

  public void setFiles(List<DownloadFile> files) {
    this.files = files;
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
