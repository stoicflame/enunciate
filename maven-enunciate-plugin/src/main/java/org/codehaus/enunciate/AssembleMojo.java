package org.codehaus.enunciate;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.ProjectAssemblyModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal assemble
 * @phase process-sources
 * @requiresDependencyResolution compile
 * @executionStrategy once-per-session

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
   * @parameter expression="${enunciate.target}"
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
    protected void initModules(Collection<DeploymentModule> modules) throws EnunciateException, IOException {
      super.initModules(modules);

      for (DeploymentModule module : modules) {
        if (module instanceof ProjectAssemblyModule) {
          ProjectAssemblyModule assemblyModule = (ProjectAssemblyModule) module;
          assemblyModule.setDoCompile(false);
          assemblyModule.setDoLibCopy(false);
          assemblyModule.setDoPackage(false);
          assemblyModule.setBuildDir(new File(project.getBasedir(), webappDirectory));
        }
      }
    }
  }
}
