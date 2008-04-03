/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.spring_app;

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
