package com.webcohesion.enunciate.mojo;

import com.webcohesion.enunciate.module.DocumentationProviderModule;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

import java.io.File;
import java.util.Locale;

/**
 * Generates the Enunciate documentation, including any client-side libraries.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "docs", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class DocsBaseMojo extends ConfigMojo implements MavenReport {

  /**
   * The directory where the docs are put.
   */
  @Parameter( defaultValue = "${project.reporting.outputDirectory}", property = "enunciate.docsDir", required = true )
  protected String docsDir;

  /**
   * The name of the subdirectory where the documentation is put.
   */
  @Parameter
  protected String docsSubdir = "apidocs";

  /**
   * The name of the index page.
   */
  @Parameter
  protected String indexPageName;

  /**
   * The name of the docs report.
   */
  @Parameter( defaultValue = "Web Service API")
  protected String reportName;

  /**
   * The description of the docs report.
   */
  @Parameter( defaultValue = "Web Service API Documentation" )
  protected String reportDescription;

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

  public void generate(Sink sink, Locale locale) throws MavenReportException {
    //first get rid of the empty page the the site plugin puts there, in order to make room for the documentation.
    new File(getReportOutputDirectory(), this.indexPageName == null ? "index.html" : this.indexPageName).delete();

    // for some reason, when running in the "site" lifecycle, the context classloader
    // doesn't get set up the same way it does when doing the default lifecycle
    // so we have to set it up manually here.
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      execute();
    }
    catch (MojoExecutionException e) {
      throw new MavenReportException("Unable to generate web service documentation report", e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(old);
    }
  }

  public String getOutputName() {
    String indexName = "index";
    if (this.indexPageName != null) {
      if (this.indexPageName.indexOf('.') > 0) {
        indexName = this.indexPageName.substring(0, this.indexPageName.indexOf('.'));
      }
      else {
        indexName = this.indexPageName;
      }
    }
    return this.docsSubdir == null ? indexName : (this.docsSubdir + "/" + indexName);
  }

  public String getName(Locale locale) {
    return this.reportName;
  }

  public String getCategoryName() {
    return CATEGORY_PROJECT_REPORTS;
  }

  public String getDescription(Locale locale) {
    return this.reportDescription;
  }

  public void setReportOutputDirectory(File outputDirectory) {
    this.docsDir = outputDirectory.getAbsolutePath();
  }

  public File getReportOutputDirectory() {
    File outputDir = new File(this.docsDir);
    if (this.docsSubdir != null) {
      outputDir = new File(outputDir, this.docsSubdir);
    }
    return outputDir;
  }

  public boolean isExternalReport() {
    return true;
  }

  public boolean canGenerateReport() {
    return true;
  }
}