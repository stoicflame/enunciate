package org.codehaus.enunciate;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.spring_app.SpringAppDeploymentModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal assemble
 * @phase generate-sources
 * @requiresDependencyResolution compile

 * @author Ryan Heaton
 */
public class AssembleMojo extends ConfigMojo {

  /**
   * The directory where the webapp is built.  If using this goal along with "war" packaging, this must be configured to be the
   * same value as the "webappDirectory" parameter to the war plugin.
   *
   * @parameter expression="target/${project.build.finalName}"
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
   * @parameter
   */
  private String stepTo = null;

  private AssembleOnlyMavenSpecificEnunciate enunciate = null;

  @Override
  public void execute() throws MojoExecutionException {
    if (forceWarPackaging && !"war".equalsIgnoreCase(this.project.getPackaging())) {
      throw new MojoExecutionException("The 'assemble' goal requires 'war' packaging.");
    }

    super.execute();

    Enunciate.Stepper stepper = (Enunciate.Stepper) getPluginContext().get(ConfigMojo.ENUNCIATE_STEPPER_PROPERTY);
    if (stepper == null) {
      throw new MojoExecutionException("No stepper found in the project!");
    }

    Enunciate.Target target = Enunciate.Target.PACKAGE;
    if (stepTo == null) {
      stepTo = project.getProperties().getProperty("enunciate.target");
    }

    if (stepTo != null) {
      target = Enunciate.Target.valueOf(stepTo.toUpperCase());
    }

    try {
      stepper.stepTo(target);
      stepper.close();
    }
    catch (Exception e) {
      throw new MojoExecutionException("Problem assembling the enunciate app.", e);
    }

    //now we have to include the generated sources into the compile source roots.
    for (File additionalRoot : this.enunciate.getAdditionalSourceRoots()) {
      addSourceDirToProject(additionalRoot);
    }
  }

  @Override
  protected MavenSpecificEnunciate loadMavenSpecificEnunciate(Set<File> sourceFiles) {
    enunciate = new AssembleOnlyMavenSpecificEnunciate(sourceFiles);
    return enunciate;
  }

  /**
   * A maven-specific enunciate mechanism that performs assembly-only (skips compilation/packaging of the war).
   */
  protected class AssembleOnlyMavenSpecificEnunciate extends MavenSpecificEnunciate {

    public AssembleOnlyMavenSpecificEnunciate(Collection<File> rootDirs) {
      super(rootDirs);
    }

    @Override
    protected void onInitSpringAppDeploymentModule(SpringAppDeploymentModule springAppModule) throws IOException {
      super.onInitSpringAppDeploymentModule(springAppModule);

      springAppModule.setDoCompile(false);
      springAppModule.setDoLibCopy(false);
      springAppModule.setDoPackage(false);
      springAppModule.setBuildDir(new File(project.getBasedir(), webappDirectory));
    }
  }
}
