package com.webcohesion.enunciate.mojo;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import org.apache.maven.plugin.install.InstallFileMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Install an Enunciate-generated artifact as if it were in its own project.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "install-artifact", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class InstallArtifactBaseMojo extends InstallFileMojo {

  /**
   * The Maven project reference.
   */
  @Parameter( defaultValue = "${project}", required = true, readonly = true)
  protected MavenProject project;

  @Parameter( required = true )
  protected String enunciateArtifactId;

  /**
   * NOTE: this parameter isn't really used by this plugin; it's only declared to override the 'required' state of the field it hides.
   */
  @Parameter
  protected File file;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.enunciateArtifactId == null) {
      throw new MojoExecutionException("An enunciate artifact id must be supplied.");
    }

    Enunciate enunciate = (Enunciate) getPluginContext().get(ConfigMojo.ENUNCIATE_PROPERTY);
    if (enunciate == null) {
      throw new MojoExecutionException("No enunciate mechanism found in the project!");
    }

    com.webcohesion.enunciate.artifacts.Artifact artifact = enunciate.findArtifact(this.enunciateArtifactId);
    if (artifact == null) {
      throw new MojoExecutionException("Unknown Enunciate artifact: " + this.enunciateArtifactId + ".");
    }

    File mainArtifact = null;
    File sources = null;
    File javadocs = null;
    if (artifact instanceof ClientLibraryArtifact) {
      for (com.webcohesion.enunciate.artifacts.Artifact childArtifact : ((ClientLibraryArtifact) artifact).getArtifacts()) {
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

      if (mainArtifact == null) {
        throw new MojoExecutionException("Unable to install artifact '" + this.enunciateArtifactId + "': no binaries available. This is likely because the '" + artifact.getModule() + "' module didn't compile the binaries.");
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
      String artifactName = mainArtifact != null ? mainArtifact.getName() : null;
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
