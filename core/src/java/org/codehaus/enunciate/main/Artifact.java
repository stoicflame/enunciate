package org.codehaus.enunciate.main;

import java.io.File;
import java.io.IOException;

/**
 * An artifact that can be exported by Enunciate.
 *
 * @author Ryan Heaton
 */
public interface Artifact extends Comparable<Artifact> {

  /**
   * The id of the artifact.
   *
   * @return The id of the artifact.
   */
  String getId();

  /**
   * The name of the module that published this artifact.
   *
   * @return The name of the module that published this artifact.
   */
  String getModule();

  /**
   * Exports this artifact to the specified file or directory.
   *
   * @param fileOrDirectory The file or directory to export to.
   * @param enunciate The enunciate mechanism to use for utilities and properties as necessary.
   * @throws java.io.IOException If an error occurs exporting it.
   */
  void exportTo(File fileOrDirectory, Enunciate enunciate) throws IOException;
  
}
