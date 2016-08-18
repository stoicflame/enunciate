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
package com.webcohesion.enunciate.module;

import java.io.File;
import java.util.List;

/**
 * Interface for a deployment module that extends the project.
 *
 * @author Ryan Heaton
 */
public interface ProjectExtensionModule extends EnunciateModule {

  /**
   * Any additional project source roots to add to the project.
   *
   * @return Any additional project source roots to add to the project.
   */
  List<File> getProjectSources();

  /**
   * Any additional project test source roots to add to the project.
   *
   * @return Any additional project test source roots to add to the project.
   */
  List<File> getProjectTestSources();

  /**
   * Any additional project resource directories to add to the project.
   *
   * @return Any additional project resource directories to add to the project.
   */
  List<File> getProjectResourceDirectories();

  /**
   * Any additional project resource directories to add to the project.
   *
   * @return Any additional project resource directories to add to the project.
   */
  List<File> getProjectTestResourceDirectories();
}
