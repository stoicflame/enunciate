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

package org.codehaus.enunciate.modules;

import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.apache.commons.digester.RuleSet;

import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * Basic stub for a deployment module.  Provides methods for each step.
 *
 * @author Ryan Heaton
 */
public class BasicDeploymentModule implements DeploymentModule {

  protected Enunciate enunciate;
  private boolean disabled;
  private File specifiedGenerateDir = null;
  private File specifiedCompileDir = null;
  private File specifiedBuildDir = null;
  private File specifiedPackageDir = null;
  private final Set<String> aliases = new TreeSet<String>();

  /**
   * @return "basic"
   */
  public String getName() {
    return getClass().getSimpleName();
  }

  /**
   * The aliases (modifiable).
   *
   * @return The aliases (modifiable).
   */
  public Set<String> getAliases() {
    return this.aliases;
  }

  /**
   * @return null
   */
  public Validator getValidator() {
    return null;
  }

  // Inherited.
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Disable (or enable) this deployment module.
   *
   * @param disabled true to disable, false to enable.
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * Sets the enunciate mechanism.
   *
   * @param enunciate The enunciate mechanism.
   */
  public void init(Enunciate enunciate) throws EnunciateException {
    this.enunciate = enunciate;
  }

  /**
   * The enunciate mechanism.
   *
   * @return The enunciate mechanism.
   */
  public Enunciate getEnunciate() {
    return enunciate;
  }

  /**
   * Calls the step methods as necessary.
   *
   * @param target The step.
   */
  public void step(Enunciate.Target target) throws EnunciateException, IOException {
    switch (target) {
      case GENERATE:
        doGenerate();
        break;
      case BUILD:
        doBuild();
        break;
      case COMPILE:
        doCompile();
        break;
      case PACKAGE:
        doPackage();
        break;
    }
  }

  /**
   * Default implementation is a no-op.
   */
  protected void doGenerate() throws EnunciateException, IOException {
  }

  /**
   * Default implementation is a no-op.
   */
  protected void doBuild() throws EnunciateException, IOException {
  }

  /**
   * Default implementation is a no-op.
   */
  protected void doCompile() throws EnunciateException, IOException {
  }

  /**
   * Default implementation is a no-op.
   */
  protected void doPackage() throws EnunciateException, IOException {
  }

  /**
   * Default implementation is a no-op.
   */
  public void close() throws EnunciateException {
  }

  /**
   * Default implementation returns null.
   *
   * @return null.
   */
  public RuleSet getConfigurationRules() {
    return null;
  }

  /**
   * @return 0
   */
  public int getOrder() {
    return 0;
  }

  /**
   * The generate directory for this module.  Defaults to &lt;enunciate generate dir&gt;/&lt;module name&gt;
   *
   * @return The generate directory for this module.
   */
  public File getGenerateDir() {
    return this.specifiedGenerateDir == null ? new File(getEnunciate().getGenerateDir(), getName()) : this.specifiedGenerateDir;
  }

  /**
   * Set the generate dir for this module.
   *
   * @param generateDir The generate dir for this module.
   */
  public void setGenerateDir(File generateDir) {
    this.specifiedGenerateDir = generateDir;
  }

  /**
   * The compile directory for this module.  Defaults to &lt;enunciate compile dir&gt;/&lt;module name&gt;
   *
   * @return The compile directory for this module.
   */
  public File getCompileDir() {
    return this.specifiedCompileDir == null ? new File(getEnunciate().getCompileDir(), getName()) : this.specifiedCompileDir;
  }

  /**
   * Set the compile dir for this module.
   *
   * @param compileDir The compile dir for this module.
   */
  public void setCompileDir(File compileDir) {
    this.specifiedCompileDir = compileDir;
  }

  /**
   * The build directory for this module.  Defaults to &lt;enunciate build dir&gt;/&lt;module name&gt;
   *
   * @return The build directory for this module.
   */
  public File getBuildDir() {
    return this.specifiedBuildDir == null ? new File(getEnunciate().getBuildDir(), getName()) : this.specifiedBuildDir;
  }

  /**
   * Set the build dir for this module.
   *
   * @param buildDir The build dir for this module.
   */
  public void setBuildDir(File buildDir) {
    this.specifiedBuildDir = buildDir;
  }

  /**
   * The package directory for this module.  Defaults to &lt;enunciate package dir&gt;/&lt;module name&gt;
   *
   * @return The package directory for this module.
   */
  public File getPackageDir() {
    return this.specifiedPackageDir == null ? new File(getEnunciate().getPackageDir(), getName()) : this.specifiedPackageDir;
  }

  /**
   * Set the package dir for this module.
   *
   * @param packageDir The package dir for this module.
   */
  public void setPackageDir(File packageDir) {
    this.specifiedPackageDir = packageDir;
  }

  /**
   * Handle an info-level message.
   *
   * @param message The info message.
   * @param formatArgs The format args of the message.
   */
  public void info(String message, Object... formatArgs) {
    if (this.enunciate != null) {
      this.enunciate.info('[' + getName() + "] " + message, formatArgs);
    }
  }

  /**
   * Handle a debug-level message.
   *
   * @param message The debug message.
   * @param formatArgs The format args of the message.
   */
  public void debug(String message, Object... formatArgs) {
    if (this.enunciate != null) {
      this.enunciate.debug('[' + getName() + "] " + message, formatArgs);
    }
  }

  /**
   * Handle a warn-level message.
   *
   * @param message The warn message.
   * @param formatArgs The format args of the message.
   */
  public void warn(String message, Object... formatArgs) {
    if (this.enunciate != null) {
      this.enunciate.warn('[' + getName() + "] " + message, formatArgs);
    }
  }

}
