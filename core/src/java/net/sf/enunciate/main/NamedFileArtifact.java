package net.sf.enunciate.main;

import java.io.File;

/**
 * A file artifact that supports a name.
 * 
 * @author Ryan Heaton
 */
public class NamedFileArtifact extends FileArtifact implements NamedArtifact {

  public NamedFileArtifact(String module, String id, File file) {
    super(module, id, file);
  }

  /**
   * The name of the artifact.
   *
   * @return The name of the artifact.
   */
  public String getName() {
    return getFile().getName();
  }

}
