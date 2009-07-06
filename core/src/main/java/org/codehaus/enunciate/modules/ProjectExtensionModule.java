package org.codehaus.enunciate.modules;

import java.io.File;
import java.util.List;

/**
 * Interface for a deployment module that extends the project.
 *
 * @author Ryan Heaton
 */
public interface ProjectExtensionModule extends DeploymentModule {

  /**
   * Any additional project source roots to add to the project.
   *
   * @return Any additional project source roots to add to the project.
   */
  List<File> getProjectSources();

  /**
   * Any additional project test source roots to add to the project.
   *
   * @return Any additional project test source roots to add to the project.
   */
  List<File> getProjectTestSources();

  /**
   * Any additional project resource directories to add to the project.
   *
   * @return Any additional project resource directories to add to the project.
   */
  List<File> getProjectResourceDirectories();

  /**
   * Any additional project resource directories to add to the project.
   *
   * @return Any additional project resource directories to add to the project.
   */
  List<File> getProjectTestResourceDirectories();
}
