package org.codehaus.enunciate.modules;

/**
 * A deployment module that is aware of the project title.
 *
 * @author Ryan Heaton
 */
public interface ProjectTitleAware extends DeploymentModule {

  /**
   * Set the title of the project
   *
   * @param title The title.
   */
  public void setTitle(String title);

  /**
   * Set the title for this project iff it hasn't already been set.
   *
   * @param title The title.
   */
  public void setTitleConditionally(String title);
}