package org.codehaus.enunciate;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.main.Enunciate;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
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
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression= "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  private ArtifactFactory artifactFactory;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression= "${component.org.apache.maven.artifact.metadata.ArtifactMetadataSource}"
   * @required
   * @readonly
   */
  private ArtifactMetadataSource artifactMetadataSource;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
   * @required
   * @readonly
   */
  private ArtifactResolver artifactResolver;

  /**
   * Location of the local repository.
   *
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  private ArtifactRepository localRepository;

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
    webAppConfig.setDir(new File(project.getBasedir(), webappDirectory).getAbsolutePath());
  }

  @Override
  protected String lookupSourceJar(File pathEntry) {
    for (Artifact projectDependency : this.projectDependencies) {
      if (pathEntry.equals(projectDependency.getFile())) {
        getLog().debug("Attemping to lookup source artifact for " + projectDependency.toString() + "...");
        try {
          Artifact sourceArtifact = this.artifactFactory.createArtifactWithClassifier(projectDependency.getGroupId(), projectDependency.getArtifactId(),
                                                                                      projectDependency.getVersion(), projectDependency.getType(), "sources");
          this.artifactResolver.resolve(sourceArtifact, this.project.getRemoteArtifactRepositories(), this.localRepository);
          String path = sourceArtifact.getFile().getAbsolutePath();
          getLog().debug("Source artifact found at " + path + ".");
          return path;
        }
        catch (Exception e) {
          getLog().debug("Unable to lookup source artifact for path entry " + pathEntry, e);
          return null;
        }
      }
    }

    return null;
  }
}
