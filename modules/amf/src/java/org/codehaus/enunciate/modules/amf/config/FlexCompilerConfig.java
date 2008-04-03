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

package org.codehaus.enunciate.modules.amf.config;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Configuration element for extra flex compiler options.
 *
 * @author Ryan Heaton
 */
public class FlexCompilerConfig {

  private String flexCompileCommand = "flex2.tools.Compiler";
  private String swcCompileCommand = "flex2.tools.Compc";
  private String contextRoot = null;
  private final List<String> JVMArgs = new ArrayList<String>();
  private final List<String> args = new ArrayList<String>();
  private File flexConfig = null;
  private String locale = null;
  private Boolean optimize = null;
  private Boolean debug;
  private Boolean profile = null;
  private Boolean strict = null;
  private Boolean useNetwork = null;
  private Boolean incremental = null;
  private Boolean warnings = null;
  private Boolean showActionscriptWarnings = null;
  private Boolean showBindingWarnings = null;
  private Boolean showDeprecationWarnings = null;
  private final List<License> licenses = new ArrayList<License>();

  /**
   * The SWC compile command.
   *
   * @return The SWC compile command.
   */
  public String getSwcCompileCommand() {
    return swcCompileCommand;
  }

  /**
   * The SWC compile command.
   *
   * @param swcCompileCommand The SWC compile command.
   */
  public void setSwcCompileCommand(String swcCompileCommand) {
    this.swcCompileCommand = swcCompileCommand;
  }

  /**
   * The flex compiler command.
   *
   * @return The flex compiler command.
   */
  public String getFlexCompileCommand() {
    return flexCompileCommand;
  }

  /**
   * The flex compiler command.
   *
   * @param flexCompileCommand The flex compiler command.
   */
  public void setFlexCompileCommand(String flexCompileCommand) {
    this.flexCompileCommand = flexCompileCommand;
  }

  public String getContextRoot() {
    return contextRoot;
  }

  public void setContextRoot(String contextRoot) {
    this.contextRoot = contextRoot;
  }

  /**
   * Get the list of JVM args to be passed to the flex compiler.
   *
   * @return the list of JVM args to be passed to the flex compiler.
   */
  public List<String> getJVMArgs() {
    return JVMArgs;
  }

  /**
   * Add a compile JVM arg to the list of those passed to the compiler.
   *
   * @param arg The argument to pass.
   */
  public void addJVMArg(String arg) {
    this.JVMArgs.add(arg);
  }

  /**
   * Extra args for the Flex compile.
   *
   * @return Extra args for the Flex compile.
   */
  public List<String> getArgs() {
    return args;
  }

  /**
   * Extra args for the Flex compile.
   *
   * @param arg Extra args for the Flex compile.
   */
  public void addArg(String arg) {
    this.args.add(arg);
  }

  /**
   * Get the licenses for the compiler.
   *
   * @return The licenses.
   */
  public List<License> getLicenses() {
    return licenses;
  }

  /**
   * Add a license.
   *
   * @param license The license to add.
   */
  public void addLicense(License license) {
    this.licenses.add(license);
  }

  public File getFlexConfig() {
    return flexConfig;
  }

  public void setFlexConfig(File flexConfig) {
    this.flexConfig = flexConfig;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public Boolean getOptimize() {
    return optimize;
  }

  public void setOptimize(Boolean optimize) {
    this.optimize = optimize;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public Boolean getProfile() {
    return profile;
  }

  public void setProfile(Boolean profile) {
    this.profile = profile;
  }

  public Boolean getStrict() {
    return strict;
  }

  public void setStrict(Boolean strict) {
    this.strict = strict;
  }

  public Boolean getUseNetwork() {
    return useNetwork;
  }

  public void setUseNetwork(Boolean useNetwork) {
    this.useNetwork = useNetwork;
  }

  public Boolean getIncremental() {
    return incremental;
  }

  public void setIncremental(Boolean incremental) {
    this.incremental = incremental;
  }

  public Boolean getWarnings() {
    return warnings;
  }

  public void setWarnings(Boolean warnings) {
    this.warnings = warnings;
  }

  public Boolean getShowActionscriptWarnings() {
    return showActionscriptWarnings;
  }

  public void setShowActionscriptWarnings(Boolean showActionscriptWarnings) {
    this.showActionscriptWarnings = showActionscriptWarnings;
  }

  public Boolean getShowBindingWarnings() {
    return showBindingWarnings;
  }

  public void setShowBindingWarnings(Boolean showBindingWarnings) {
    this.showBindingWarnings = showBindingWarnings;
  }

  public Boolean getShowDeprecationWarnings() {
    return showDeprecationWarnings;
  }

  public void setShowDeprecationWarnings(Boolean showDeprecationWarnings) {
    this.showDeprecationWarnings = showDeprecationWarnings;
  }

}
