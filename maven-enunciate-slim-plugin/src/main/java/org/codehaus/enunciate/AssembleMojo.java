package org.codehaus.enunciate;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.main.Enunciate;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal assemble
 * @phase process-sources
 * @requiresDependencyResolution test

 * @author Ryan Heaton
 */
public class AssembleMojo extends ConfigMojo {

  public static final String SOURCE_JAR_MAP_PROPERTY = "urn:" + AssembleMojo.class.getName() + "#source_jars";

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
    Map<File, String> sourceJars = (Map<File, String>) getPluginContext().get(SOURCE_JAR_MAP_PROPERTY);
    if (sourceJars == null) {
      sourceJars = new TreeMap<File, String>();
      getPluginContext().put(SOURCE_JAR_MAP_PROPERTY, sourceJars);
    }

    if (sourceJars.containsKey(pathEntry)) {
      return sourceJars.get(pathEntry);
    }

    String sourceJar = null;
    for (Artifact projectDependency : ((Set<Artifact>) this.project.getArtifacts())) {
      if (pathEntry.equals(projectDependency.getFile())) {
        if (skipSourceJarLookup(projectDependency)) {
          getLog().debug("Skipping the source lookup for " + projectDependency.toString() + "...");
        }

        getLog().debug("Attemping to lookup source artifact for " + projectDependency.toString() + "...");
        try {
          Artifact sourceArtifact = this.artifactFactory.createArtifactWithClassifier(projectDependency.getGroupId(), projectDependency.getArtifactId(),
                                                                                      projectDependency.getVersion(), projectDependency.getType(), "sources");
          this.artifactResolver.resolve(sourceArtifact, this.project.getRemoteArtifactRepositories(), this.localRepository);
          String path = sourceArtifact.getFile().getAbsolutePath();
          getLog().debug("Source artifact found at " + path + ".");
          sourceJar = path;
          break;
        }
        catch (Exception e) {
          getLog().debug("Unable to lookup source artifact for path entry " + pathEntry, e);
          break;
        }
      }
    }

    sourceJars.put(pathEntry, sourceJar);

    return sourceJar;
  }

  /**
   * Whether to skip the source-jar lookup for the given dependency.
   *
   * @param projectDependency The dependency.
   * @return Whether to skip the source-jar lookup for the given dependency.
   */
  protected boolean skipSourceJarLookup(Artifact projectDependency) {
    String groupId = String.valueOf(projectDependency.getGroupId());
    return groupId.startsWith("com.sun.") //skip com.sun.*
      || "com.sun".equals(groupId) //skip com.sun
      || groupId.startsWith("javax."); //skip "javax.*"
  }
}
