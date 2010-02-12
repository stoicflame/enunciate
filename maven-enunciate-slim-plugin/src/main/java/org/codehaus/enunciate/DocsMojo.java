package org.codehaus.enunciate;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.DocumentationAwareModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

/**
 * Assembles the whole Enunciate app without compilation or packaging of the war.
 * For use with the "war" packaging.
 *
 * @goal docs
 * @phase process-sources
 * @requiresDependencyResolution test
 *
 * @author Ryan Heaton
 */
public class DocsMojo extends ConfigMojo implements MavenReport {

  /**
   * The directory where the docs are put.
   *
   * @parameter expression="${project.reporting.outputDirectory}/wsdocs"
   * @required
   */
  private String docsDir;

  /**
   * The name of the subdirectory where the documentation is put.
   *
   * @parameter
   */
  private String docsSubdir;

  /**
   * The name of the index page.
   *
   * @parameter
   */
  private String indexPageName;

  /**
   * The target to step to.
   *
   * @parameter expression="${enunciate.target}"
   */
  private String stepTo = null;

  /**
   * The name of the docs report.
   *
   * @parameter
   */
  private String reportName = "Web Service API";

  /**
   * The description of the docs report.
   *
   * @parameter
   */
  private String reportDescription = "Web Service API Documentation";

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

  @Override
  protected EnunciateConfiguration createEnunciateConfiguration() {
    EnunciateConfiguration config = super.createEnunciateConfiguration();
    WebAppConfig webAppConfig = config.getWebAppConfig();
    if (webAppConfig == null) {
      webAppConfig = new WebAppConfig();
      config.setWebAppConfig(webAppConfig);
    }
    webAppConfig.setDisabled(true);
    return config;
  }

  public void generate(Sink sink, Locale locale) throws MavenReportException {
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

  /**
   * A maven-specific enunciate mechanism that performs assembly-only (skips compilation/packaging of the war).
   */
  protected class DocsOnlyMavenSpecificEnunciate extends MavenSpecificEnunciate {

    public DocsOnlyMavenSpecificEnunciate(Collection<File> rootDirs) {
      super(rootDirs);
    }

    @Override
    protected void initModules(Collection<DeploymentModule> modules) throws EnunciateException, IOException {
      super.initModules(modules);

      for (DeploymentModule module : modules) {
        if ("docs".equals(module.getName())) {
          if (docsSubdir != null) {
            ((DocumentationAwareModule)module).setDocsDir(docsSubdir);
          }

          if (indexPageName != null) {
            ((DocumentationAwareModule)module).setIndexPageName(indexPageName);
          }
        }
      }
    }

    @Override
    protected void doClose() throws EnunciateException, IOException {
      super.doClose();

      FileArtifact artifact = (FileArtifact) findArtifact("docs");
      if (artifact != null) {
        getReportOutputDirectory().mkdirs();
        artifact.exportTo(getReportOutputDirectory(), this);
      }
      else {
        warn("Unable to copy the Enunciate documentation: no documentation directory artifact found.");
      }
    }
  }

}