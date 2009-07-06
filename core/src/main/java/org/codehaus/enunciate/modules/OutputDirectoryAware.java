package org.codehaus.enunciate.modules;

import java.io.File;

/**
 * A deployment module that is aware of the project output directory.
 *
 * @author Ryan Heaton
 */
public interface OutputDirectoryAware extends DeploymentModule {

  /**
   * Set the output directory.
   *
   * @param outputDir The output directory.
   */
  public void setOutputDirectory(File outputDir);
}
