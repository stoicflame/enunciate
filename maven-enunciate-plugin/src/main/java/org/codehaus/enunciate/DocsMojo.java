package org.codehaus.enunciate;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.spring_app.SpringAppDeploymentModule;
import org.codehaus.enunciate.modules.docs.DocumentationDeploymentModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal docs
 * @phase process-sources
 * @requiresDependencyResolution compile

 * @author Ryan Heaton
 */
public class DocsMojo extends ConfigMojo {

  /**
   * The directory where the docs are put.
   *
   * @parameter expression="${project.build.directory}/docs"
   * @required
   */
  private String docsDir;

  /**
   * The target to step to.
   *
   * @parameter expression="${enunciate.target}"
   */
  private String stepTo = null;

  @Override
  public void execute() throws MojoExecutionException {
    super.execute();

    Enunciate.Stepper stepper = (Enunciate.Stepper) getPluginContext().get(ConfigMojo.ENUNCIATE_STEPPER_PROPERTY);
    if (stepper == null) {
      throw new MojoExecutionException("No stepper found in the project!");
    }

    Enunciate.Target target = Enunciate.Target.BUILD;

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
  }

  @Override
  protected MavenSpecificEnunciate loadMavenSpecificEnunciate(Set<File> sourceFiles) {
    return new DocsOnlyMavenSpecificEnunciate(sourceFiles);
  }

  /**
   * A maven-specific enunciate mechanism that performs assembly-only (skips compilation/packaging of the war).
   */
  protected class DocsOnlyMavenSpecificEnunciate extends MavenSpecificEnunciate {

    public DocsOnlyMavenSpecificEnunciate(Collection<File> rootDirs) {
      super(rootDirs);
    }

    @Override
    protected void onInitSpringAppDeploymentModule(SpringAppDeploymentModule springAppModule) throws IOException {
      super.onInitSpringAppDeploymentModule(springAppModule);
      springAppModule.setDisabled(true); //disable the spring-app module.
    }

    @Override
    protected void onInitDocsModule(DocumentationDeploymentModule docsModule) {
      super.onInitDocsModule(docsModule);
      docsModule.setDocsDir(null);
      docsModule.setBuildDir(new File(docsDir));
    }
  }

}