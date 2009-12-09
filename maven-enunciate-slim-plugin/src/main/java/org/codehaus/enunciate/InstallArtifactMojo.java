package org.codehaus.enunciate;

import org.apache.maven.plugin.install.InstallFileMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.enunciate.main.*;
import org.codehaus.enunciate.main.Artifact;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Extension of the intall plugin to install an Enunciate-generated artifact.
 *
 * @goal install-artifact
 * @phase install
 * @extendsPlugin install
 * @extendsGoal install-file
 *
 * @author Ryan Heaton
 */
public class InstallArtifactMojo extends InstallFileMojo {

  /**
   * The Maven project reference.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * @parameter
   * @required
   */
  private String enunciateArtifactId;

  /**
   * NOTE: this parameter isn't really used by this plugin; it's only declared to override the 'required' state of the field it hides.
   * @parameter
   */
  private File file;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.enunciateArtifactId == null) {
      throw new MojoExecutionException("An enunciate artifact id must be supplied.");
    }

    Enunciate enunciate = (Enunciate) getPluginContext().get(ConfigMojo.ENUNCIATE_PROPERTY);
    if (enunciate == null) {
      throw new MojoExecutionException("No enunciate mechanism found in the project!");
    }

    org.codehaus.enunciate.main.Artifact artifact = enunciate.findArtifact(this.enunciateArtifactId);
    if (artifact == null) {
      throw new MojoExecutionException("Unknown Enunciate artifact: " + this.enunciateArtifactId + ".");
    }

    File mainArtifact = null;
    File sources = null;
    File javadocs = null;
    if (artifact instanceof ClientLibraryArtifact) {
      for (Artifact childArtifact : ((ClientLibraryArtifact) artifact).getArtifacts()) {
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
    else if (artifact instanceof FileArtifact) {
      mainArtifact = ((FileArtifact) artifact).getFile();
    }
    else {
      try {
        mainArtifact = enunciate.createTempFile(this.enunciateArtifactId, "artifact");
      }
      catch (IOException e) {
        throw new MojoExecutionException("Unable to create a temp file.", e);
      }
    }

    if (this.packaging == null) {
      String artifactName = mainArtifact != null ? mainArtifact.getName() :
        sources != null ? sources.getName() :
        javadocs != null ? javadocs.getName() :
        null;
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

    if (this.groupId == null) {
      this.groupId = this.project.getGroupId();
    }

    if (this.artifactId == null) {
      this.artifactId = this.project.getArtifactId() + "-client";
    }

    if (this.version == null) {
      this.version = this.project.getVersion();
    }

    setPrivateField("file", mainArtifact);
    setPrivateField("sources", sources);
    setPrivateField("javadoc", javadocs);

    super.execute();
  }

  private void setPrivateField(String fieldName, Object value) {
    Field field = findField(InstallFileMojo.class, fieldName);
    if (field == null) {
      throw new IllegalStateException("No such field: " + fieldName);
    }

    field.setAccessible(true);
    try {
      field.set(this, value);
    }
    catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private Field findField(Class clazz, String fieldName) {
    if (Object.class.equals(clazz)) {
      return null;
    }
    else {
      for (Field field : clazz.getDeclaredFields()) {
        if (field.getName().equals(fieldName)) {
          return field;
        }
      }
    }
    return findField(clazz.getSuperclass(), fieldName);
  }
}
