package org.codehaus.enunciate.modules;

/**
 * A deployment module that is aware of the project version.
 *
 * @author Ryan Heaton
 */
public interface ProjectVersionAware extends DeploymentModule {

  /**
   * Set the version for this project.
   *
   * @param version The version.
   */
  public void setProjectVersion(String version);
}