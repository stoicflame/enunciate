/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.PathSummary;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PathSummaryImpl implements PathSummary {

  private final String path;
  private final Set<String> methods;
  private final Set<String> styles;

  public PathSummaryImpl(String path, Set<String> methods, Set<String> styles) {
    this.path = path;
    this.methods = methods;
    this.styles = styles;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<String> getMethods() {
    return methods;
  }

  @Override
  public Set<String> getStyles() {
    return styles;
  }
}
