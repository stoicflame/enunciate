package org.codehaus.enunciate.main;

import java.io.IOException;
import java.io.InputStream;

/**
 * An entry (class, source file, config file) on the classpath.
 *
 * @author Ryan Heaton
 */
public interface ClasspathResource {

  /**
   * The path (slash-separated directories) of the classpath entry.
   *
   * @return The path of the classpath entry.
   */
  String getPath();

  /**
   * Read the classpath entry.
   *
   * @return The stream to the classpath entry.
   */
  InputStream read() throws IOException;
}
