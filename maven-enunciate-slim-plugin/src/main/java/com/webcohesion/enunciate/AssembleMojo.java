package com.webcohesion.enunciate;

import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.main.Enunciate;

import java.io.File;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @author Ryan Heaton
 * @goal assemble
 * @phase process-sources
 * @requiresDependencyResolution test
 */
public class AssembleMojo extends ConfigMojo {

  /**
   * The directory where the webapp is built.  If using this goal along with "war" packaging, this must be configured to be the
   * same value as the "webappDirectory" parameter to the war plugin.
   *
   * @parameter expression="${enunciate.webappDirectory}" default-value="${project.build.directory}/${project.build.finalName}"
   * @required
   */
  private String webappDirectory;

  /**
   * Whether to force the "packaging" of the project to be "war" packaging.
   *
   * @parameter
   */
  private boolean forceWarPackaging = true;

  /**
   * The target to step to.
   *
   * @parameter expression="${enunciate.target}"
   */
  private String stepTo = null;

  @Override
  public void execute() throws MojoExecutionException {
    if (skipEnunciate) {
      getLog().info("Skipping enunciate per configuration.");
      return;
    }

    if (forceWarPackaging && !"war".equalsIgnoreCase(this.project.getPackaging())) {
      throw new MojoExecutionException("The 'assemble' goal requires 'war' packaging.");
    }

    super.execute();

    Enunciate.Stepper stepper = (Enunciate.Stepper) getPluginContext().get(ConfigMojo.ENUNCIATE_STEPPER_PROPERTY);
    if (stepper == null) {
      throw new MojoExecutionException("No stepper found in the project!");
    }

    Enunciate.Target target = Enunciate.Target.PACKAGE;

    if (stepTo != null) {
      target = Enunciate.Target.valueOf(stepTo.toUpperCase());
    }

    synchronized(ThreadSafety.lock) {
      try {
        stepper.stepTo(target);
        stepper.close();
      }
      catch (Exception e) {
        throw new MojoExecutionException("Problem assembling the enunciate app.", e);
      }
    }

    Enunciate enunciate = (Enunciate) getPluginContext().get(ConfigMojo.ENUNCIATE_PROPERTY);
    if (enunciate == null) {
      throw new MojoExecutionException("No enunciate mechanism found in the project!");
    }
    //now we have to include the generated sources into the compile source roots.
    for (File additionalRoot : enunciate.getAdditionalSourceRoots()) {
      addSourceDirToProject(additionalRoot);
    }
  }

  @Override
  protected void postProcessConfig(EnunciateConfiguration config) {
    super.postProcessConfig(config);
    WebAppConfig webAppConfig = config.getWebAppConfig();
    if (webAppConfig == null) {
      webAppConfig = new WebAppConfig();
      config.setWebAppConfig(webAppConfig);
    }
    webAppConfig.setDoCompile(false);
    webAppConfig.setDoLibCopy(false);
    webAppConfig.setDoPackage(false);
    webAppConfig.setDir(new File(webappDirectory).getAbsolutePath());
  }

}
