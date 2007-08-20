/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.main;

/**
 * A dependency for an artifact.
 *
 * @author Ryan Heaton
 */
public interface ArtifactDependency {

  /**
   * The dependency id.
   *
   * @return The dependency id.
   */
  String getId();

  /**
   * The dependency version.
   *
   * @return The dependency version.
   */
  String getVersion();

  /**
   * The type of the artifact (e.g. "jar", "dll").
   *
   * @return The type of the artifact.
   */
  String getArtifactType();

  /**
   * A URL for looking up the dependency.
   *
   * @return A URL for looking up the dependency.
   */
  String getURL();

  /**
   * A description of the dependency.
   *
   * @return A description of the dependency.
   */
  String getDescription();
}