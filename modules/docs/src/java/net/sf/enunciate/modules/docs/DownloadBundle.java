package net.sf.enunciate.modules.docs;

import net.sf.enunciate.main.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Arrays;

/**
 * An artifact bundle the contains information about a download file.
 *
 * @author Ryan Heaton
 */
public class DownloadBundle extends BaseArtifact implements ArtifactBundle, NamedArtifact {

  private String name;
  private String description;
  private final NamedFileArtifact file;

  public DownloadBundle(String module, String id, File file) {
    super(module, id);

    this.file = new NamedFileArtifact(module, id, file);
  }

  /**
   * Exports this bundle to the specified file or directory.
   *
   * @param fileOrDirectory The file or directory to which to export this bundle.
   * @param enunciate The enunciated utilities to use.
   */
  public void exportTo(File fileOrDirectory, Enunciate enunciate) throws IOException {
    this.file.exportTo(fileOrDirectory, enunciate);
  }

  /**
   * There's only one bundled artifact: the download file.
   *
   * @return There's only one bundled artifact: the download file.
   */
  public Collection<? extends Artifact> getArtifacts() {
    return Arrays.asList(file);
  }

  /**
   * The name of this bundle.
   *
   * @return The name of this bundle.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of this bundle.
   *
   * @param name The name of this bundle.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The description of this bundle.
   *
   * @return The description of this bundle.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The description of this bundle.
   *
   * @param description The description of this bundle.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The modification date of the file.
   *
   * @return The modification date of the file.
   */
  public Date getCreated() {
    return new Date(this.file.getFile().lastModified());
  }
}
