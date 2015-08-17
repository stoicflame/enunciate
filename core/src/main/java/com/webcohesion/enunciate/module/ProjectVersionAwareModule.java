package com.webcohesion.enunciate.module;

/**
 * A deployment module that is aware of the project version.
 *
 * @author Ryan Heaton
 */
public interface ProjectVersionAwareModule extends EnunciateModule {

  /**
   * Set the version for this project.
   *
   * @param version The version.
   */
  public void setProjectVersion(String version);
}