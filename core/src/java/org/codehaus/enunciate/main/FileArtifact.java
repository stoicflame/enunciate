package org.codehaus.enunciate.main;

import java.io.File;
import java.io.IOException;

/**
 * A file artifact.
 *
 * @author Ryan Heaton
 */
public class FileArtifact extends BaseArtifact {

  private final File file;
  private String description;

  public FileArtifact(String module, String id, File file) {
    super(module, id);
    this.file = file;
  }

  /**
   * The file for this artifact.
   *
   * @return The file for this artifact.
   */
  public File getFile() {
    return file;
  }

  /**
   * Exports this artifact to the specified file.  If this file is a directory,
   * the directory will be zipped up.
   *
   * @param file The file to export to.
   */
  public void exportTo(File file, Enunciate enunciate) throws IOException {
    if (!this.file.exists()) {
      throw new IOException("Unable to export non-existing file " + this.file);
    }

    if (this.file.isDirectory()) {
      if (file.exists() && file.isDirectory()) {
        enunciate.copyDir(this.file, file);
      }
      else {
        enunciate.zip(this.file, file);
      }
    }
    else {
      if (file.exists() && file.isDirectory()) {
        enunciate.copyFile(this.file, new File(file, this.file.getName()));
      }
      else {
        enunciate.copyFile(this.file, file);
      }
    }
  }

  /**
   * The description of this file artifact.
   *
   * @return The description of this file artifact.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The description of this file artifact.
   *
   * @param description The description of this file artifact.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
