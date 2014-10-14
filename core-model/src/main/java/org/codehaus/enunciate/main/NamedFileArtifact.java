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

package org.codehaus.enunciate.main;

import java.io.File;

/**
 * A file artifact that supports a name.
 * 
 * @author Ryan Heaton
 */
public class NamedFileArtifact extends FileArtifact implements NamedArtifact {

  public NamedFileArtifact(String module, String id, File file) {
    super(module, id, file);
  }

  /**
   * The name of the artifact.
   *
   * @return The name of the artifact.
   */
  public String getName() {
    return getFile().getName();
  }

}
