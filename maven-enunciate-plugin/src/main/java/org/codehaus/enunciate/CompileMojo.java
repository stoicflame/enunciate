package org.codehaus.enunciate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.enunciate.main.Enunciate;

import java.util.Properties;

/**
 * Goal which performs the "compile" step of an Enunciate build process.
 *
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class CompileMojo extends AbstractMojo {

  /**
   * The Maven project reference.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  public void execute() throws MojoExecutionException {
    Enunciate.Stepper stepper = (Enunciate.Stepper) getPluginContext().get(ConfigMojo.ENUNCIATE_STEPPER_PROPERTY);
    if (stepper == null) {
      throw new MojoExecutionException("No stepper found in the project!");
    }

    if (stepper.getNextTarget() == null) {
      throw new MojoExecutionException("Uninitialized or stale Enunciate stepper.");
    }

    try {
      stepper.stepTo(Enunciate.Target.COMPILE);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Problem with compile step.", e);
    }
  }
}
