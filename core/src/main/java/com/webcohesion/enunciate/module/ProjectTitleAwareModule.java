package com.webcohesion.enunciate.module;

/**
 * A deployment module that is aware of the project title.
 *
 * @author Ryan Heaton
 */
public interface ProjectTitleAwareModule extends EnunciateModule {

  /**
   * Set the default title for the project.
   *
   * @param title The default title.
   */
  public void setDefaultTitle(String title);
}