package com.webcohesion.enunciate.mojo;

import com.webcohesion.enunciate.module.DocumentationProviderModule;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

/**
 * Assembles the Enunciate documentation.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "assemble", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class AssembleBaseMojo extends ConfigMojo {

  /**
   * The directory where the docs are put.
   */
  @Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}", property = "enunciate.docsDir", required = true )
  protected String docsDir;

  /**
   * The name of the subdirectory where the documentation is put.
   */
  @Parameter
  protected String docsSubdir;

  /**
   * The directory where the webapp is built.  If using this goal along with "war" packaging, this must be configured to be the
   * same value as the "webappDirectory" parameter to the war plugin.
   */
  @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", property = "enunciate.webappDirectory")
  protected String webappDirectory;

  /**
   * Whether to force the "packaging" of the project to be "war" packaging.
   */
  @Parameter(defaultValue = "true")
  protected boolean forceWarPackaging = true;

  @Override
  protected void applyAdditionalConfiguration(EnunciateModule module) {
    super.applyAdditionalConfiguration(module);

    if (module instanceof DocumentationProviderModule) {
      DocumentationProviderModule docsProvider = (DocumentationProviderModule) module;
      docsProvider.setDefaultDocsDir(new File(this.docsDir));
      if (this.docsSubdir != null) {
        docsProvider.setDefaultDocsSubdir(this.docsSubdir);
      }
    }
  }

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

    //todo: figure out what else needs to happen. Do we even need this mojo anymore?
  }

}
