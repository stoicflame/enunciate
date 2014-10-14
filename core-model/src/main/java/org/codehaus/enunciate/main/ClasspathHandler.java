package org.codehaus.enunciate.main;

import java.io.File;

/**
 * Event handler for classpath scanning.
 *
 * @author Ryan Heaton
 */
public interface ClasspathHandler {

  /**
   * Start an entry on the classpath.
   *
   * @param pathEntry The path entry.
   */
  void startPathEntry(File pathEntry);

  /**
   * Handle a classpath resource.
   * 
   * @param resource The classpath resource to handle.
   */
  void handleResource(ClasspathResource resource);

  /**
   * End an entry on the classpath.
   *
   * @param pathEntry The entry on the classpath that we're ending.
   * @return Whether to attempt to look up the sources for the specified path entry.
   */
  boolean endPathEntry(File pathEntry);
}
