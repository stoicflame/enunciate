package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.main.BaseArtifact;
import net.sf.enunciate.main.Enunciate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A client-side libarary artifact.
 *
 * @author Ryan Heaton
 */
public class ClientLibraryArtifact extends BaseArtifact {

  private final String name;
  private Date created;
  private String platform;
  private String description;
  private final HashMap<File, String> files = new HashMap<File, String>();

  public ClientLibraryArtifact(String module, String id, String name) {
    super(module, id);
    this.name = name;
    this.created = new Date();
  }

  /**
   * Zips up all files in this library and writes the archive to the specified file.
   *
   * @param file The file to write to.
   * @param enunciate The utilities to use.
   */
  public void exportTo(File file, Enunciate enunciate) throws IOException {
    file.getParentFile().mkdirs();

    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
    byte[] buffer = new byte[2 * 1024]; //buffer of 2K should be fine.
    for (File entry : this.files.keySet()) {
      out.putNextEntry(new ZipEntry(entry.getName()));
      
      FileInputStream in = new FileInputStream(file);
      int len;
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }

      // Complete the entry
      out.closeEntry();
      in.close();
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
   * The files (with associated descriptions) for this artifact.
   *
   * @return The files (with associated descriptions) for this artifact.
   */
  public Map<File, String> getFiles() {
    return files;
  }

  /**
   * Gets the description for the file specified by name.
   *
   * @param filename The filename.
   * @return The description.
   */
  public String getFileDescription(String filename) {
    for (File file : this.files.keySet()) {
      if (file.getName().equals(filename)) {
        return this.files.get(file);
      }
    }

    return null;
  }

  /**
   * Adds a file to this library.
   *
   * @param file The file to add to the librar.
   * @param description The description of the file.
   */
  public void addFile(File file, String description) {
    this.files.put(file, description);
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
