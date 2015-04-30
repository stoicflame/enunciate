package org.codehaus.enunciate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Extension of the intall plugin to install an Enunciate-generated artifact.
 *
 * @author Ryan Heaton
 * @goal deploy-artifact
 * @phase deploy
 */
public class DeployArtifactMojo extends AbstractMojo implements Contextualizable {

  private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.+)::(.+)");

  /**
   * @parameter expression="${component.org.apache.maven.artifact.deployer.ArtifactDeployer}"
   * @required
   * @readonly
   */
  private ArtifactDeployer deployer;

  /**
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  private ArtifactRepository localRepository;

  /**
   * Maven ProjectHelper
   *
   * @component
   * @readonly
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter
   * @required
   */
  private String enunciateArtifactId;

  /**
   * @parameter expression="${project.distributionManagementArtifactRepository}"
   * @readonly
   */
  private ArtifactRepository deploymentRepository;

  /**
   * Specifies an alternative repository to which the project artifacts should be deployed ( other
   * than those specified in &lt;distributionManagement&gt; ).
   * <br/>
   * Format: id::layout::url
   *
   * @parameter expression="${altDeploymentRepository}"
   */
  private String altDeploymentRepository;

  /**
   * Contextualized.
   */
  private PlexusContainer container;

  /**
   * GroupId of the artifact to be deployed.  Retrieved from POM file if specified.
   *
   * @parameter expression="${project.groupId}"
   */
  private String groupId;

  /**
   * ArtifactId of the artifact to be deployed.  Retrieved from POM file if specified.
   *
   * @parameter expression="${project.artifactId}-client"
   */
  private String artifactId;

  /**
   * Version of the artifact to be deployed.  Retrieved from POM file if specified.
   *
   * @parameter expression="${project.version}"
   */
  private String version;

  /**
   * Type of the artifact to be deployed.  Retrieved from POM file if specified.
   *
   * @parameter
   */
  private String packaging;

  /**
   * Description passed to a generated POM file (in case of generatePom=true)
   *
   * @parameter
   */
  private String description;

  /**
   * Component used to create an artifact
   *
   * @component
   */
  private ArtifactFactory artifactFactory;

  /**
   * Location of an existing POM file to be deployed alongside the artifact(s).
   *
   * @parameter
   */
  private File pomFile;

  /**
   * Upload a POM for the artifact(s) to be deployed.  Will generate a default POM if none is supplied with the pomFile argument.
   *
   * @parameter default-value="true"
   */
  private boolean generatePom;

  /**
   * Add classifier to the artifact
   *
   * @parameter expression="${classifier}";
   */
  private String classifier;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.enunciateArtifactId == null) {
      throw new MojoExecutionException("An enunciate artifact id must be supplied.");
    }

    Enunciate enunciate = (Enunciate) getPluginContext().get(ConfigMojo.ENUNCIATE_PROPERTY);
    if (enunciate == null) {
      throw new MojoExecutionException("No enunciate mechanism found in the project!");
    }

    org.codehaus.enunciate.main.Artifact enunciateArtifact = enunciate.findArtifact(this.enunciateArtifactId);
    if (enunciateArtifact == null) {
      throw new MojoExecutionException("Unknown Enunciate artifact: " + this.enunciateArtifactId + ".");
    }

    File mainArtifact = null;
    File sources = null;
    File javadocs = null;
    if (enunciateArtifact instanceof ClientLibraryArtifact) {
      for (org.codehaus.enunciate.main.Artifact childArtifact : ((ClientLibraryArtifact) enunciateArtifact).getArtifacts()) {
        if (childArtifact instanceof FileArtifact) {
          ArtifactType artifactType = ((FileArtifact) childArtifact).getArtifactType();
          if (artifactType != null) {
            switch (artifactType) {
              case binaries:
                mainArtifact = ((FileArtifact) childArtifact).getFile();
                break;
              case sources:
                sources = ((FileArtifact) childArtifact).getFile();
                break;
              case javadocs:
                javadocs = ((FileArtifact) childArtifact).getFile();
                break;
            }
          }
        }
      }
    }
    else if (enunciateArtifact instanceof FileArtifact) {
      mainArtifact = ((FileArtifact) enunciateArtifact).getFile();
    }
    else {
      try {
        mainArtifact = enunciate.createTempFile(this.enunciateArtifactId, "artifact");
        enunciateArtifact.exportTo(mainArtifact, enunciate);
      }
      catch (IOException e) {
        throw new MojoExecutionException("Unable to create a temp file.", e);
      }
    }

    if (mainArtifact == null) {
      if (sources != null) {
        mainArtifact = sources;
        sources = null;
      }
    }

    if (mainArtifact == null) {
      throw new MojoExecutionException("Unable to determine the file to deploy from enunciate artifact " + enunciateArtifactId + ".");
    }

    // Process the supplied POM (if there is one)
    Model model = null;
    if (pomFile != null) {
      generatePom = false;
      model = readModel(pomFile);
      processModel(model);
    }

    if (this.packaging == null) {
      String artifactName = mainArtifact.getName();
      if (artifactName != null) {
        int dotIndex = artifactName.indexOf('.');
        if (dotIndex > 0 && (dotIndex + 1 < artifactName.length())) {
          this.packaging = artifactName.substring(dotIndex + 1);
        }
      }
    }

    if (this.packaging == null) {
      throw new MojoExecutionException("Unable to determine the packaging of enunciate artifact " + enunciateArtifactId + ". Please specify it in the configuration.");
    }

    if (model == null) {
      model = new Model();
      model.setModelVersion("4.0.0");
      model.setGroupId(this.groupId);
      model.setArtifactId(this.artifactId);
      model.setVersion(this.version);
      model.setPackaging(this.packaging);
      model.setDescription(this.description);
    }

    ArtifactRepository repo = getDeploymentRepository();

    String protocol = repo.getProtocol();

    if (protocol.equals("scp")) {
      File sshFile = new File(System.getProperty("user.home"), ".ssh");

      if (!sshFile.exists()) {
        sshFile.mkdirs();
      }
    }

    Artifact artifact = this.artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, packaging, classifier);

    // Upload the POM if requested, generating one if need be
    if (generatePom) {
      ArtifactMetadata metadata = new ProjectArtifactMetadata(artifact, generatePomFile(model));
      artifact.addMetadata(metadata);
    }
    else {
      ArtifactMetadata metadata = new ProjectArtifactMetadata(artifact, pomFile);
      artifact.addMetadata(metadata);
    }

    try {
      getDeployer().deploy(mainArtifact, artifact, repo, this.localRepository);

      if (sources != null || javadocs != null) {
        MavenProject project = new MavenProject(model);
        project.setArtifact(artifact);
        if (sources != null) {
          //we have to do it this way because of classloading issues.
          this.projectHelper.attachArtifact(project, artifact.getType(), "sources", sources);
          getDeployer().deploy(sources, (Artifact) project.getAttachedArtifacts().get(0), repo, this.localRepository);
        }

        if (javadocs != null) {
          //we have to do it this way because of classloading issues.
          this.projectHelper.attachArtifact(project, artifact.getType(), "javadoc", javadocs);
          getDeployer().deploy(javadocs, (Artifact) project.getAttachedArtifacts().get(0), repo, this.localRepository);
        }
      }
    }
    catch (ArtifactDeploymentException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  public ArtifactDeployer getDeployer() {
    return deployer;
  }

  //
  private ArtifactRepository getDeploymentRepository() throws MojoExecutionException, MojoFailureException {
    if (deploymentRepository == null && altDeploymentRepository == null) {
      String msg =
        "Deployment failed: repository element was not specified in the pom inside"
          + " distributionManagement element or in -DaltDeploymentRepository=id::layout::url parameter";

      throw new MojoExecutionException(msg);
    }

    ArtifactRepository repo = null;

    if (altDeploymentRepository != null) {
      getLog().info("Using alternate deployment repository " + altDeploymentRepository);

      Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(altDeploymentRepository);

      if (!matcher.matches()) {
        throw new MojoFailureException(altDeploymentRepository, "Invalid syntax for repository.",
                                       "Invalid syntax for alternative repository. Use \"id::layout::url\".");
      }
      else {
        String id = matcher.group(1).trim();
        String layout = matcher.group(2).trim();
        String url = matcher.group(3).trim();

        ArtifactRepositoryLayout repoLayout;
        try {
          repoLayout = (ArtifactRepositoryLayout) container.lookup(ArtifactRepositoryLayout.ROLE, layout);
        }
        catch (ComponentLookupException e) {
          throw new MojoExecutionException("Cannot find repository layout: " + layout, e);
        }

        repo = new DefaultArtifactRepository(id, url, repoLayout);
      }
    }

    if (repo == null) {
      repo = deploymentRepository;
    }

    return repo;
  }

  public void contextualize(Context context)
    throws ContextException {
    this.container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
  }

  /**
   * Process the supplied pomFile to get groupId, artifactId, version, and packaging
   *
   * @throws NullPointerException if model is <code>null</code>
   */
  private void processModel(Model model) {
    Parent parent = model.getParent();

    if (this.groupId == null) {
      if (parent != null && parent.getGroupId() != null) {
        this.groupId = parent.getGroupId();
      }
      if (model.getGroupId() != null) {
        this.groupId = model.getGroupId();
      }
    }
    if (this.artifactId == null && model.getArtifactId() != null) {
      this.artifactId = model.getArtifactId();
    }
    if (this.version == null && model.getVersion() != null) {
      this.version = model.getVersion();
    }
    if (this.packaging == null && model.getPackaging() != null) {
      this.packaging = model.getPackaging();
    }
  }

  /**
   * Extract the Model from the specified file.
   *
   * @param pomFile
   * @return
   * @throws MojoExecutionException if the file doesn't exist of cannot be read.
   */
  protected Model readModel(File pomFile)
    throws MojoExecutionException {

    if (!pomFile.exists()) {
      throw new MojoExecutionException("Specified pomFile does not exist");
    }

    Reader reader = null;
    try {
      reader = new FileReader(pomFile);
      MavenXpp3Reader modelReader = new MavenXpp3Reader();
      return modelReader.read(reader);
    }
    catch (FileNotFoundException e) {
      throw new MojoExecutionException("Error reading specified POM file: " + e.getMessage(), e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Error reading specified POM file: " + e.getMessage(), e);
    }
    catch (XmlPullParserException e) {
      throw new MojoExecutionException("Error reading specified POM file: " + e.getMessage(), e);
    }
    finally {
      IOUtil.close(reader);
    }
  }

  private File generatePomFile(Model model)
    throws MojoExecutionException {
    FileWriter fw = null;
    try {
      File tempFile = File.createTempFile("mvninstall", ".pom");
      tempFile.deleteOnExit();


      fw = new FileWriter(tempFile);
      new MavenXpp3Writer().write(fw, model);

      return tempFile;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Error writing temporary pom file: " + e.getMessage(), e);
    }
    finally {
      IOUtil.close(fw);
    }
  }

}
