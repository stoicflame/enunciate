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

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration element for a GWT app.
 *
 * @author Ryan Heaton
 */
public class GWTApp {

  public enum JavaScriptStyle {

    OBF,

    OBFUSCATED,

    PRETTY,

    DETAILED

  }

  private String name = "";
  private String srcDir;
  private JavaScriptStyle javascriptStyle = JavaScriptStyle.OBF;
  private final List<GWTAppModule> modules = new ArrayList<GWTAppModule>();

  /**
   * The name of this GWT app.
   *
   * @return The name of this GWT app.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of this GWT app.
   *
   * @param name The name of this GWT app.
   */
  public void setName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("A name must be specified for a GWT app, even if it's the empty string.");
    }
    this.name = name;
  }

  /**
   * The source directory of the app (relative to the configuration file).
   *
   * @return The source directory.
   */
  public String getSrcDir() {
    return srcDir;
  }

  /**
   * The source directory of the app (relative to the configuration file).
   *
   * @param srcDir The source directory of the app (relative to the configuration file).
   */
  public void setSrcDir(String srcDir) {
    this.srcDir = srcDir;
  }

  /**
   * The javascript style.
   *
   * @return The javascript style.
   */
  public JavaScriptStyle getJavascriptStyle() {
    return javascriptStyle;
  }

  /**
   * The javascript style.
   *
   * @param javascriptStyle The javascript style.
   */
  public void setJavascriptStyle(JavaScriptStyle javascriptStyle) {
    this.javascriptStyle = javascriptStyle;
  }

  /**
   * The javascript style value.
   *
   * @param javascriptStyle The javascript style value.
   */
  public void setJavascriptStyleValue(String javascriptStyle) {
    this.javascriptStyle = JavaScriptStyle.valueOf(javascriptStyle);
  }

  /**
   * The set of modules to compile.
   *
   * @return The set of modules to compile.
   */
  public List<GWTAppModule> getModules() {
    return modules;
  }

  /**
   * The module to add.
   *
   * @param module The module to add.
   */
  public void addModule(GWTAppModule module) {
    if (module.getName() == null) {
      throw new IllegalArgumentException("A module name must be specified with the 'name' attribute.");
    }
    
    this.modules.add(module);
  }
}
