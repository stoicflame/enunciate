package org.codehaus.enunciate.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A classpath entry that is packaged up in a jar.
 *
 * @author Ryan Heaton
 */
public class JarClasspathResource implements ClasspathResource {

  private final JarFile jarFile;
  private final JarEntry entry;

  public JarClasspathResource(JarFile jarFile, JarEntry entry) {
    this.jarFile = jarFile;
    this.entry = entry;
  }

  public String getPath() {
    return this.entry.getName();
  }

  public InputStream read() throws IOException {
    return this.jarFile.getInputStream(this.entry);
  }
}