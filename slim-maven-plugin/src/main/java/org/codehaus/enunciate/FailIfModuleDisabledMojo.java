package org.codehaus.enunciate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.enunciate.main.Enunciate;

/**
 * Mojo that fails if a module is disabled. This module assumes an instance of {@link org.codehaus.enunciate.ConfigMojo} has already been executed.
 *
 * @author Ryan Heaton
 * @goal failIfModuleDisabled
 * @requiresDependencyResolution runtime
 */
public class FailIfModuleDisabledMojo extends AbstractMojo {

  /**
   * Whether to skip this test.
   * @parameter
   */
  private boolean skip = false;

  /**
   * The module name.
   *
   * @parameter
   * @required
   */
  private String moduleName  = null;

  /**
   * The failure message.
   *
   * @parameter
   */
  private String message = null;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!skip) {
      if (moduleName == null) {
        throw new MojoExecutionException("A module name must be specified in order to check whether it's disabled.");
      }

      Enunciate enunciate = (Enunciate) getPluginContext().get(ConfigMojo.ENUNCIATE_PROPERTY);
      if (enunciate == null) {
        throw new MojoExecutionException("Enunciate mechanism not initialized yet.");
      }
      else if (!enunciate.isModuleEnabled(moduleName)) {
        String message = "Enunciate module '" + moduleName + "' is disabled. See the console output for details.";

        if (this.message != null) {
          message = this.message;
        }
        
        throw new MojoExecutionException(message);
      }
    }
  }
}
