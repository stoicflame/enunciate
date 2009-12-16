package org.codehaus.enunciate.modules;

/**
 * @author Ryan Heaton
 */
public interface DocumentationAwareModule extends ProjectTitleAware {

  /**
   * The title of the documentation.
   *
   * @return The title of the documentation.
   */
  String getTitle();

  /**
   * The title of the documentation.
   *
   * @param title The title of the documentation.
   */
  void setTitle(String title);

  /**
   * The subdirectory in the web application where the documentation will be put.
   *
   * @return The subdirectory in the web application where the documentation will be put.
   */
  String getDocsDir();

  /**
   * The subdirectory in the web application where the documentation will be put.
   *
   * @param docsDir The subdirectory in the web application where the documentation will be put.
   */
  void setDocsDir(String docsDir);

  /**
   * The name of the index page.
   *
   * @return The name of the index page.
   */
  String getIndexPageName();

  /**
   * The name of the index page.
   *
   * @param indexPageName The name of the index page.
   */
  void setIndexPageName(String indexPageName);
}
