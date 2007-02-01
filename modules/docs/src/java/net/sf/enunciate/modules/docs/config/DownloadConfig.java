package net.sf.enunciate.modules.docs.config;

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
  private File file;

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
  public File getFile() {
    return file;
  }

  /**
   * The file to expose as a download. (Ignored if "artifact" is set.)
   *
   * @param file The file to expose as a download. (Ignored if "artifact" is set.)
   */
  public void setFile(File file) {
    this.file = file;
  }
}
