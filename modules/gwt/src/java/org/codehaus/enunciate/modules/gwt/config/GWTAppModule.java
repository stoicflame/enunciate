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

package org.codehaus.enunciate.modules.gwt.config;

/**
 * A module for a GWT app.
 *
 * @author Ryan Heaton
 */
public class GWTAppModule {

  private String name;
  private String outputPath;

  /**
   * The name of the module.
   *
   * @return The name of the module.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the module.
   *
   * @param name The name of the module.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The output path of the module.
   *
   * @return The output path of the module.
   */
  public String getOutputPath() {
    return outputPath;
  }

  /**
   * The output path of the module.
   *
   * @param outputPath The output path of the module.
   */
  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }
}
