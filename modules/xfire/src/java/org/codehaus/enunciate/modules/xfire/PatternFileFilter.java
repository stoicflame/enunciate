package org.codehaus.enunciate.modules.xfire;

import org.springframework.util.PathMatcher;

import java.io.FileFilter;
import java.io.File;
import java.net.URI;

/**
 * A file filter that filters based on a pattern.
 *
 * @author Ryan Heaton
 */
public class PatternFileFilter implements FileFilter {

  private final File basedir;
  private final PathMatcher matcher;
  private final String pattern;

  public PatternFileFilter(File basedir, String pattern, PathMatcher matcher) {
    this.basedir = basedir;
    this.matcher = matcher;
    this.pattern = pattern;
  }

  public boolean accept(File pathname) {
    URI relativeURI = this.basedir.toURI().relativize(pathname.toURI());
    String relativePath = relativeURI.toString();
    return this.matcher.match(this.pattern, relativePath);
  }
}
