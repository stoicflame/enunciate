package com.webcohesion.enunciate.module;

/**
 * A deployment module that is aware of the project title.
 *
 * @author Ryan Heaton
 */
public interface ProjectTitleAware extends EnunciateModule {

  /**
   * Set the title for this project iff it hasn't already been set.
   *
   * @param title The title.
   */
  public void setTitleConditionally(String title);
}