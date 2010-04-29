package org.codehaus.enunciate.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A classpath entry that is a file on the filesystem.
 *
 * @author Ryan Heaton
 */
public class FileClasspathResource implements ClasspathResource {

  private final String path;
  private final File file;

  public FileClasspathResource(File file, File pathRoot) {
    this.path = pathRoot.toURI().relativize(file.toURI()).getPath();
    this.file = file;
  }

  public String getPath() {
    return this.path;
  }

  public InputStream read() throws IOException {
    return new FileInputStream(this.file);
  }
}
