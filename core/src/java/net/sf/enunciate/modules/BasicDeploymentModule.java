package net.sf.enunciate.modules;

import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.main.Enunciate;
import org.apache.commons.digester.RuleSet;

import java.io.IOException;
import java.io.File;

/**
 * Basic stub for a deployment module.  Provides methods for each step.
 *
 * @author Ryan Heaton
 */
public class BasicDeploymentModule implements DeploymentModule {

  protected Enunciate enunciate;
  private boolean disabled;

  /**
   * @return "basic"
   */
  public String getName() {
    return getClass().getSimpleName();
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
    return new File(getEnunciate().getGenerateDir(), getName());
  }

  /**
   * The compile directory for this module.  Defaults to &lt;enunciate compile dir&gt;/&lt;module name&gt;
   *
   * @return The compile directory for this module.
   */
  public File getCompileDir() {
    return new File(getEnunciate().getCompileDir(), getName());
  }

  /**
   * The build directory for this module.  Defaults to &lt;enunciate build dir&gt;/&lt;module name&gt;
   *
   * @return The build directory for this module.
   */
  public File getBuildDir() {
    return new File(getEnunciate().getBuildDir(), getName());
  }

  /**
   * The package directory for this module.  Defaults to &lt;enunciate package dir&gt;/&lt;module name&gt;
   *
   * @return The package directory for this module.
   */
  public File getPackageDir() {
    return new File(getEnunciate().getPackageDir(), getName());
  }

}
