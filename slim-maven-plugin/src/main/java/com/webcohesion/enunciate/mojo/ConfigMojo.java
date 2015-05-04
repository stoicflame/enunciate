  package com.webcohesion.enunciate.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.ProjectExtensionModule;
import com.webcohesion.enunciate.module.ProjectTitleAware;
import com.webcohesion.enunciate.module.ProjectVersionAware;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Goal which initializes an Enunciate build process.
 */
@SuppressWarnings ( "unchecked" )
@Mojo( name = "config", defaultPhase = LifecyclePhase.VALIDATE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class ConfigMojo extends AbstractMojo {

  public static final String ENUNCIATE_PROPERTY = "com.webcohesion.enunciate.mojo.ConfigMojo#ENUNCIATE_PROPERTY";

  @Component
  protected MavenProjectHelper projectHelper;

  @Component
  protected MavenFileFilter configFilter;

  @Component
  protected ArtifactFactory artifactFactory;

  @Component
  protected ArtifactResolver artifactResolver;

  @Parameter( defaultValue = "${project}", required = true, readonly = true )
  protected MavenProject project;

  @Parameter( defaultValue = "${plugin.artifacts}", required = true, readonly = true)
  protected Collection<org.apache.maven.artifact.Artifact> pluginDependencies;

  @Parameter( defaultValue = "${session}", required = true, readonly = true )
  protected MavenSession session;

  @Parameter( defaultValue = "${localRepository}", required = true, readonly = true)
  protected ArtifactRepository localRepository;

  @Parameter( defaultValue = "${project.build.directory}", required = true)
  protected File outputDir = null;

  /**
   * The enunciate artifacts.
   */
  @Parameter
  protected Artifact[] artifacts;

  /**
   * The enunciate configuration file.
   */
  @Parameter
  protected File configFile = null;

  /**
   * The output directory for Enunciate.
   */
  @Parameter( defaultValue = "${project.build.directory}/enunciate", property = "enunciate.build.directory")
  protected File buildDir = null;

  /**
   * The Enunciate exports.
   */
  @Parameter
  protected Map<String, String> exports = new HashMap<String, String>();

  /**
   * The include patterns.
   */
  @Parameter
  protected String[] includes;

  /**
   * The exclude patterns.
   */
  @Parameter
  protected String[] excludes;

  /**
   * List of compiler arguments.
   */
  @Parameter
  protected String[] compilerArgs;

  /**
   * Compiler -source version parameter
   */
  @Parameter( name = "source", property = "maven.compiler.source" )
  private String compilerSource = null;
  
  /**
   * Compiler -target version parameter
   */
  @Parameter( name = "target", property = "maven.compiler.target" )
  private String compilerTarget = null;

  /**
   * The -encoding argument for the Java compiler
   */
  @Parameter( name = "encoding", property = "encoding", defaultValue = "${project.build.sourceEncoding}")
  private String sourceEncoding = null;

  /**
   * A flag used to disable enunciate. This is primarily intended for usage from the command line to occasionally adjust the build.
   */
  @Parameter(defaultValue = "false", property = "enunciate.skip")
  protected boolean skipEnunciate;

  public void execute() throws MojoExecutionException {
    if (skipEnunciate) {
      getLog().info("Skipping enunciate per configuration.");
      return;
    }

    Enunciate enunciate = new Enunciate();

    //set up the logger.
    enunciate.setLogger(new MavenEnunciateLogger());

    //set the build dir.
    enunciate.setBuildDir(this.buildDir);

    //load the config.
    EnunciateConfiguration config = enunciate.getConfiguration();
    File configFile = this.configFile;
    if (configFile == null) {
      configFile = new File(project.getBasedir(), "enunciate.xml");
    }

    if (configFile.exists()) {
      getLog().info("Using enunciate configuration at " + configFile.getAbsolutePath());

      try {
        loadConfig(enunciate, configFile);
        config.setBase(this.configFile.getParentFile());
      }
      catch (Exception e) {
        throw new MojoExecutionException("Problem with enunciate config file " + configFile, e);
      }
    }

    //set the default configured label.
    config.setDefaultLabel(project.getArtifactId());

    //set the class paths.
    enunciate.setApiClasspath(buildRuntimeClasspath());
    enunciate.setBuildClasspath(buildBuildClasspath());

    //set the compiler arguments.
    List<String> compilerArgs = new ArrayList<String>();
    String sourceVersion = findSourceVersion();
    if (sourceVersion != null) {
    	compilerArgs.add("-source");
      compilerArgs.add(sourceVersion);
    }

    String targetVersion = findTargetVersion();
    if (targetVersion != null) {
    	compilerArgs.add("-target");
      compilerArgs.add(targetVersion);
    }

    String sourceEncoding = findSourceEncoding();
    if (sourceEncoding != null) {
      compilerArgs.add("-encoding");
      compilerArgs.add(sourceEncoding);
    }
    enunciate.getCompilerArgs().addAll(compilerArgs);

    //includes.
    if (this.includes != null) {
      enunciate.getIncludeClasses().addAll(Arrays.asList(this.includes));
    }

    //excludes.
    if (this.excludes != null) {
      enunciate.getExcludeClasses().addAll(Arrays.asList(this.excludes));
    }

    //exports.
    if (this.exports != null) {
      for (String exportId : this.exports.keySet()) {
        String filename = this.exports.get(exportId);
        if (filename == null || "".equals(filename)) {
          throw new MojoExecutionException("Invalid (empty or null) filename for export " + exportId + ".");
        }
        File exportFile = new File(filename);
        if (!exportFile.isAbsolute()) {
          exportFile = new File(this.outputDir, filename);
        }

        enunciate.addExport(exportId, exportFile);
      }
    }

    //configure the project with the module project extensions.
    Set<String> enunciateAddedSourceDirs = new TreeSet<String>();
    List<EnunciateModule> modules = enunciate.getModules();
    for (EnunciateModule module : modules) {
      if (module.isEnabled()) {
        if (module instanceof ProjectExtensionModule) {
          ProjectExtensionModule extensions = (ProjectExtensionModule) module;
          for (File projectSource : extensions.getProjectSources()) {
            String sourceDir = projectSource.getAbsolutePath();
            enunciateAddedSourceDirs.add(sourceDir);
            if (!project.getCompileSourceRoots().contains(sourceDir)) {
              getLog().debug("Adding '" + sourceDir + "' to the compile source roots.");
              project.addCompileSourceRoot(sourceDir);
            }
          }

          for (File testSource : extensions.getProjectTestSources()) {
            project.addTestCompileSourceRoot(testSource.getAbsolutePath());
          }

          for (File resourceDir : extensions.getProjectResourceDirectories()) {
            Resource restResource = new Resource();
            restResource.setDirectory(resourceDir.getAbsolutePath());
            project.addResource(restResource);
          }

          for (File resourceDir : extensions.getProjectTestResourceDirectories()) {
            Resource resource = new Resource();
            resource.setDirectory(resourceDir.getAbsolutePath());
            project.addTestResource(resource);
          }
        }

        if (project.getName() != null && !"".equals(project.getName().trim()) && module instanceof ProjectTitleAware) {
          ((ProjectTitleAware) module).setTitleConditionally(project.getName());
        }
        if (project.getVersion() != null && !"".equals(project.getVersion().trim()) && module instanceof ProjectVersionAware) {
          ((ProjectVersionAware) module).setProjectVersion(project.getVersion());
        }
      }
    }

    //add any new source directories to the project.
    Set<File> sourceDirs = new HashSet<File>();
    Collection<String> sourcePaths = (Collection<String>) project.getCompileSourceRoots();
    for (String sourcePath : sourcePaths) {
      File sourceDir = new File(sourcePath);
      if (!enunciateAddedSourceDirs.contains(sourceDir.getAbsolutePath())) {
        sourceDirs.add(sourceDir);
      }
      else {
        getLog().info(sourceDir + " appears to be added to the source roots by Enunciate.  Excluding from original source roots....");
      }
    }

    for (File sourceDir : sourceDirs) {
      enunciate.addSourceDir(sourceDir);
    }

    postProcessConfig(enunciate);

    try {
      enunciate.run();
    }
    catch (Exception e) {
      throw new MojoExecutionException("Error invoking Enunciate.", e);
    }

    if (this.artifacts != null) {
      for (Artifact projectArtifact : artifacts) {
        if (projectArtifact.getEnunciateArtifactId() == null) {
          getLog().warn("No enunciate export id specified.  Skipping project artifact...");
          continue;
        }

        com.webcohesion.enunciate.artifacts.Artifact artifact = null;
        for (com.webcohesion.enunciate.artifacts.Artifact enunciateArtifact : enunciate.getArtifacts()) {
          if (projectArtifact.getEnunciateArtifactId().equals(enunciateArtifact.getId())
            || enunciateArtifact.getAliases().contains(projectArtifact.getEnunciateArtifactId())) {
            artifact = enunciateArtifact;
            break;
          }
        }

        if (artifact != null) {
          try {
            File tempExportFile = enunciate.createTempFile(project.getArtifactId() + "-" + projectArtifact.getClassifier(), projectArtifact.getArtifactType());
            artifact.exportTo(tempExportFile, enunciate);
            projectHelper.attachArtifact(project, projectArtifact.getArtifactType(), projectArtifact.getClassifier(), tempExportFile);
          }
          catch (IOException e) {
            throw new MojoExecutionException("Error exporting Enunciate artifact.", e);
          }
        }
        else {
          getLog().warn("Enunciate artifact '" + projectArtifact.getEnunciateArtifactId() + "' not found in the project...");
        }
      }
    }
  }

  protected String findSourceVersion() {
    //todo: find the source version configured in the maven compiler plugin.
    //todo: see http://stackoverflow.com/questions/4061386/maven-how-to-pass-parameters-between-mojos
    return this.compilerSource;
  }

  protected String findTargetVersion() {
    //todo: find the target version configured in the maven compiler plugin.
    //todo: see http://stackoverflow.com/questions/4061386/maven-how-to-pass-parameters-between-mojos
    return this.compilerTarget;
  }

  protected String findSourceEncoding() {
    //todo: find the source encoding configured in the maven compiler plugin.
    //todo: see http://stackoverflow.com/questions/4061386/maven-how-to-pass-parameters-between-mojos
    return this.sourceEncoding;
  }

  protected List<URL> buildBuildClasspath() throws MojoExecutionException {
    List<URL> classpath = new ArrayList<URL>();
    for (org.apache.maven.artifact.Artifact next : this.pluginDependencies) {
      try {
        classpath.add(next.getFile().toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new MojoExecutionException("Unable to add artifact " + next + " to the classpath.", e);
      }
    }
    return classpath;
  }

  protected List<URL> buildRuntimeClasspath() throws MojoExecutionException {
    Set<org.apache.maven.artifact.Artifact> dependencies = new LinkedHashSet<org.apache.maven.artifact.Artifact>();
    dependencies.addAll(((Set<org.apache.maven.artifact.Artifact>) this.project.getArtifacts()));
    Iterator<org.apache.maven.artifact.Artifact> it = dependencies.iterator();
    while (it.hasNext()) {
      org.apache.maven.artifact.Artifact artifact = it.next();
      String artifactScope = artifact.getScope();
      if (org.apache.maven.artifact.Artifact.SCOPE_TEST.equals(artifactScope)) {
        //remove just the test-scope artifacts from the classpath.
        it.remove();
      }
    }

    List<URL> classpath = new ArrayList<URL>(dependencies.size());
    for (org.apache.maven.artifact.Artifact projectDependency : dependencies) {
      File entry = projectDependency.getFile();

      if (skipSourceJarLookup(projectDependency)) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("Skipping the source lookup for " + projectDependency.toString() + "...");
        }
      }
      else {
        if (getLog().isDebugEnabled()) {
          getLog().debug("Attemping to lookup source artifact for " + projectDependency.toString() + "...");
        }

        try {
          org.apache.maven.artifact.Artifact sourceArtifact = this.artifactFactory.createArtifactWithClassifier(projectDependency.getGroupId(), projectDependency.getArtifactId(), projectDependency.getVersion(), projectDependency.getType(), "sources");
          this.artifactResolver.resolve(sourceArtifact, this.project.getRemoteArtifactRepositories(), this.localRepository);

          if (getLog().isDebugEnabled()) {
            getLog().debug("Source artifact found at " + sourceArtifact + ".");
          }

          entry = sourceArtifact.getFile();
       }
        catch (Exception e) {
          if (getLog().isDebugEnabled()) {
            getLog().debug("Unable to lookup source artifact for path entry " + projectDependency, e);
          }
        }
      }

      try {
        classpath.add(entry.toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new MojoExecutionException("Unable to add artifact " + projectDependency + " to the classpath.", e);
      }
    }
    return classpath;
  }

  protected void postProcessConfig(Enunciate enunciate) {
    //no-op in this implementation.
  }

  /**
   * Load the config, do filtering as needed.
   *
   * @param config     The config to load into.
   * @param configFile The config file.
   */
  protected void loadConfig(Enunciate config, File configFile) throws IOException, SAXException, MavenFilteringException {
    if (this.configFilter == null) {
      getLog().debug("No maven file filter was provided, so no filtering of the config file will be done.");
      config.loadConfiguration(configFile);
    }
    else {
      this.buildDir.mkdirs();
      File filteredConfig = File.createTempFile("enunciateConfig", ".xml", this.buildDir);
      getLog().debug("Filtering " + configFile + " to " + filteredConfig + "...");
      this.configFilter.copyFile(configFile, filteredConfig, true, this.project, new ArrayList(), true, "utf-8", this.session);
      config.loadConfiguration(filteredConfig);
    }
  }

  /**
   * Whether to skip the source-jar lookup for the given dependency.
   *
   * @param projectDependency The dependency.
   * @return Whether to skip the source-jar lookup for the given dependency.
   */
  protected boolean skipSourceJarLookup(org.apache.maven.artifact.Artifact projectDependency) {
    String groupId = String.valueOf(projectDependency.getGroupId());
    return groupId.startsWith("com.sun.") //skip com.sun.*
      || "com.sun".equals(groupId) //skip com.sun
      || groupId.startsWith("javax."); //skip "javax.*"
  }

  protected class MavenEnunciateLogger implements EnunciateLogger {
    @Override
    public void debug(String message, Object... formatArgs) {
      if (getLog().isDebugEnabled()) {
        getLog().debug(String.format(message, formatArgs));
      }
    }

    @Override
    public void info(String message, Object... formatArgs) {
      if (getLog().isInfoEnabled()) {
        getLog().info(String.format(message, formatArgs));
      }
    }

    @Override
    public void warn(String message, Object... formatArgs) {
      if (getLog().isWarnEnabled()) {
        getLog().warn(String.format(message, formatArgs));
      }
    }
  }

}
